use std::env;

mod update;
mod status;
mod updater;

fn main() {
    println!("Starting updater...");
    let args = parse_arg();
    if args.is_err() {
        println!("Error parsing arguments: {}", args.unwrap_err());
        return
    }
    let args = args.unwrap();

    status::write_status(args.output.clone(), status::UpdaterStatus {
        status: status::Status::UPDATING,
        message: None,
        exceptions: None
    }).expect("Unable to write status!");

    let update = update::get_update(args.input.as_str());
    if update.is_err() {
        let update = update.unwrap_err();
        println!("Error reading update file: {}", update);
        status::write_status(args.output.clone(), status::UpdaterStatus {
            status: status::Status::FAILURE,
            message: Some("Failed to read update file".to_string()),
            exceptions: Some(vec![update])
        }).expect("Unable to write status!");
        updater::restart(args.restart_dir.unwrap(), args.restart_command.unwrap());
        return
    }
    let update = update.unwrap();

    let final_status = updater::execute_update(update);
    status::write_status(args.output.clone(), final_status).expect("Unable to write status!");

    if args.restart_dir.is_some() && args.restart_command.is_some() {
        println!("Restarting...");
        updater::restart(args.restart_dir.unwrap(), args.restart_command.unwrap())
    }

    return
}

fn parse_arg() -> Result<Args, String> {
    let args: Vec<String> = env::args().collect();

    let input = get_arg(args.clone(), vec!["-i", "--input="]);
    if input.is_none() {
        return Err("Input not specified!".to_string());
    }
    let input = input.unwrap();

    let output = get_arg(args.clone(), vec!["-o", "--output="]);
    if output.is_none() {
        return Err("Output not specified!".to_string());
    }
    let output = output.unwrap();

    let restart = get_arg(args.clone(), vec!["-r", "--restart="]);
    let mut restart_dir: Option<String> = None;
    let mut restart_command: Option<String> = None;
    if restart.is_some() {
        let restart = restart.unwrap();
        let parts = restart.split(";").collect::<Vec<&str>>();
        if parts.len() == 2 {
            restart_dir = Some(parts.get(0).unwrap().to_string());
            restart_command = Some(parts.get(1).unwrap().to_string());
        } else {
            return Err("Invalid restart Command; use [path];[command]".to_string())
        }
    }

    return Ok(Args {
        input,
        output,
        restart_dir,
        restart_command
    });
}

fn get_arg(args: Vec<String>, keys: Vec<&str>) -> Option<String> {
    for a in args {
        for k in keys.clone() {
            if a.starts_with(k) {
                return Some(a.replace(k, ""));
            }
        }
    }
    return None;
}

#[derive(Debug)]
struct Args {
    input: String,
    output: String,
    restart_dir: Option<String>,
    restart_command: Option<String>
}
