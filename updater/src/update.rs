use std::fs;
use std::path::Path;

use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct UpdateChange {
    pub path: String,
    pub mode: UpdateMode,
    pub elements: Option<Vec<UpdateElement>>,
    pub updater: Option<bool>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub enum UpdateMode {
    FILE,
    DELETE,
    REGEX,
    LINE
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct UpdateElement {
    pub pattern: Option<String>,
    pub value: Option<String>,
    pub meta: Option<i32>,
    pub replace: Option<bool>
}

pub fn get_update(path: &str) -> Result<Vec<UpdateChange>, String> {
    let file = Path::new(path);
    if !file.is_file() {
        return Err(format!("Update file not found!: {}", file.to_string_lossy()));
    }

    let update_contents = fs::read_to_string(file);
    if update_contents.is_err() {
        return Err(format!("Unable to read update file!: {}", file.to_string_lossy()));
    }
    let update_contents = update_contents.unwrap();

    let update: serde_json::error::Result<Vec<UpdateChange>> = serde_json::from_str(update_contents.as_str());
    if update.is_err() {
        return Err(format!("Unable to parse update file!\n{}", update.unwrap_err()));
    }
    return Ok(update.unwrap());
}

pub fn delete_update(path: &str) -> Result<(), String> {
    println!("Deleting update file...");
    let file = Path::new(path);
    if file.is_file() {
        let delete = fs::remove_file(file);
        if delete.is_err() {
            return Err(format!("Unable to delete update file!: {}", file.to_string_lossy()));
        }
    }
    return Ok(());
}