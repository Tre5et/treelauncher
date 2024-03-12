use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;
use std::thread::sleep;
use std::time::Duration;
use regex::Regex;
use crate::status::{Status, UpdaterStatus};
use crate::update::{UpdateChange, UpdateElement, UpdateMode};

pub fn execute_update(update: Vec<UpdateChange>) -> UpdaterStatus {
    let mut errors: Vec<String> = Vec::new();

    let mut backed_up_files: Vec<PathBuf> = Vec::new();

    for change in update {
        let change = change.clone();
        let target = Path::new(change.path.as_str().clone());
        let update_name = format!("{}.up", change.path.clone());
        let update_file = Path::new(update_name.as_str());

        let backup = backup_file(target.to_owned());
        if backup.is_err() {
            errors.push(backup.unwrap_err());
            continue;
        }
        let backup_file = backup.unwrap();
        let backup_file = if backup_file.is_some() {
            backed_up_files.push(backup_file.clone().unwrap().to_owned());
            backup_file.unwrap()
        } else {
            Path::new("").to_path_buf()
        };

        match change.mode {
            UpdateMode::FILE => {
                let result = file_mode(target.to_owned(), update_file.to_owned());
                if result.is_err() {
                    errors.push(format!("{}: {}", result.unwrap_err(), update_file.to_string_lossy()));
                }
            },
            UpdateMode::DELETE => {
                println!("DELETE: {}", target.to_string_lossy());
                let result = fs::remove_file(target);
                if result.is_err() {
                    errors.push(format!("{}: {}", result.unwrap_err(), update_file.to_string_lossy()));
                }
            },
            UpdateMode::REGEX => {
                let result = regex_mode(target.to_owned(), backup_file.to_owned(), change.elements.unwrap());
                if result.is_err() {
                    errors.push(format!("{}: {}", result.unwrap_err(), update_file.to_string_lossy()));
                }
            },
            UpdateMode::LINE => {
                let result = line_mode(target.to_owned(), backup_file.to_owned(), change.elements.unwrap());
                if result.is_err() {
                    errors.push(format!("{}: {}", result.unwrap_err(), update_file.to_string_lossy()));
                }
            }
        }
    }

    if !errors.is_empty() {
        println!("Errors: {}", errors.clone().join("\n"));
        let restore = restore(backed_up_files);
        if restore.is_err() {
            errors.push(restore.unwrap_err());
            return UpdaterStatus {
                status: Status::FATAL,
                message: Some("Errors occurred and failed to restore backups!".to_string()),
                exceptions: Some(errors)
            }
        }
        return UpdaterStatus {
            status: Status::FAILURE,
            message: Some("Errors occurred!".to_string()),
            exceptions: Some(errors)
        }
    }

    let remove = remove_backups(backed_up_files);
    if remove.is_err() {
        return UpdaterStatus {
            status: Status::WARNING,
            message: Some("Update successful, but failed to remove backups!".to_string()),
            exceptions: Some(vec![remove.unwrap_err()])
        }
    }

    return UpdaterStatus {
        status: Status::SUCCESS,
        message: None,
        exceptions: None
    }
}

fn backup_file(file: PathBuf) -> Result<Option<PathBuf>, String> {
    if file.is_file() {
        let backup_file = Path::new(format!("{}.bak", file.to_str().unwrap()).as_str()).to_owned();
        let mut attempts = 0;
        let mut error: String = String::new();
        while attempts < 60 {
            match fs::copy(file.clone(), backup_file.clone()) {
                Ok(_) => {
                    return Ok(Some(backup_file.to_owned()));
                }
                Err(e) => {
                    attempts += 1;
                    error = e.to_string();
                    sleep(Duration::from_millis(1000))
                }
            }
        }
        return Err(format!("Failed to backup file:\n{}", error));
    }
    return Ok(None);
}

fn file_mode(target: PathBuf, update_file: PathBuf) -> Result<(), String> {
    println!("UPGRADE: {}", target.to_string_lossy());
    if !update_file.is_file() {
        return Err(format!("Update file not found!: {}", update_file.to_string_lossy()));
    }
    let rename = fs::rename(update_file, target);
    if rename.is_err() {
        return Err(format!("Failed to move file: {}", rename.unwrap_err()));
    }
    return Ok(());
}

fn line_mode(target: PathBuf, backup_file: PathBuf, elements: Vec<UpdateElement>) -> Result<(), String> {
    println!("LINE: {}", target.to_string_lossy());
    let read = fs::read_to_string(backup_file);
    if read.is_err() {
        return Err(format!("Failed to read file: {}", read.unwrap_err()));
    }
    let read = read.unwrap();

    let mut lines: Vec<String> = Vec::new();
    for line in read.lines() {
        lines.push(line.to_string());
    }

    for element in elements {
        if element.meta.is_none() {
            return Err("Meta not specified!".to_string());
        }
        let meta = element.meta.unwrap();

        let replace = if element.replace.is_none() {
            false
        } else {
            element.replace.unwrap()
        };

        let line = if meta < 0 {
            lines.len() as i32 + meta + if replace { 0 } else { 1 }
        } else {
            meta
        };

        let value = element.value;

        println!(" - {}({}) {} {}", line + 1, meta, if replace { "->" } else { "+" }, if value.is_none() { "[blank]" } else { value.as_ref().unwrap().as_str() });

        if line < 0 {
            continue;
        }

        while line >= lines.len() as i32 && (line > lines.len() as i32 || replace) {
            lines.push(String::new());
        }

        if replace {
            if value.is_none() {
                lines.remove(line as usize);
            } else {
                lines[line as usize] = value.unwrap();
            }
        } else {
            lines.insert(line as usize, value.unwrap());
        }
    }

    let write = fs::write(target, lines.join("\n"));
    if write.is_err() {
        return Err(format!("Failed to write file: {}", write.unwrap_err()));
    }
    return Ok(());
}

fn regex_mode(target: PathBuf, backup_file: PathBuf, elements: Vec<UpdateElement>) -> Result<(), String> {
    println!("REGEX: {}", target.to_string_lossy());
    let read = fs::read_to_string(backup_file);
    if read.is_err() {
        return Err(format!("Failed to read file: {}", read.unwrap_err()));
    }
    let read = read.unwrap();

    let mut result: String = read.clone();

    for element in elements {
        if element.pattern.is_none() {
            return Err("Pattern not specified!".to_string());
        }
        let pattern = element.pattern.unwrap();

        let value = if element.value.is_none() {
            String::new()
        } else {
            element.value.unwrap()
        };

        println!(" - {} -> {}", pattern, value);

        let regex = Regex::new(pattern.as_str());
        if regex.is_err() {
            return Err(format!("Invalid regex pattern: {}", regex.unwrap_err()));
        }
        let regex = regex.unwrap();

        if element.meta.is_none() {
            result = (&regex.replace_all(read.as_str(), value)).parse().unwrap();
        } else {
            let meta = element.meta.unwrap();
            let index = if meta >= 0 {
                meta
            } else {
                regex.find_iter(read.as_str()).count() as i32 + meta
            };

            if index < 0 {
                continue;
            }

            let find = regex.find_iter(&*read).nth(index as usize);
            if find.is_some() {
                let find = find.unwrap();
                result.replace_range(find.start()..find.end(), value.as_str());
            }
        }
    }

    let write = fs::write(target, result);
    if write.is_err() {
        return Err(format!("Failed to write file: {}", write.unwrap_err()));
    }
    return Ok(());
}

fn remove_backups(backed_up_files: Vec<PathBuf>) -> Result<(), String> {
    println!("Removing backups...");
    for file in backed_up_files {
        let remove = fs::remove_file(file);
        if remove.is_err() {
            return Err(format!("Failed to remove backup: {}", remove.unwrap_err()));
        }
    }
    return Ok(());
}

fn restore(backed_up_files: Vec<PathBuf>) -> Result<(), String> {
    println!("Restoring backups...");
    for file in backed_up_files {
        let name = file.to_string_lossy().to_string();
        let target = Path::new(&name[..name.len() - 4]);
        let restore = fs::rename(file, target);
        if restore.is_err() {
            return Err(format!("Failed to restore backup: {}", restore.unwrap_err()));
        }
    }
    return Ok(());
}

pub fn restart(dir: String, command: String) {
    Command::new(command).current_dir(dir).spawn().expect("Failed to restart!");
}