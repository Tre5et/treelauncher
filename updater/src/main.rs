use std::env;

mod update;
mod status;
mod updater;

fn main() {
    let args = parse_arg();
    if args.is_err() {
        println!("{}", args.unwrap_err());
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
        status::write_status(args.output.clone(), status::UpdaterStatus {
            status: status::Status::FAILURE,
            message: Some("Failed to read update file".to_string()),
            exceptions: Some(vec![update.unwrap_err()])
        }).expect("Unable to write status!");
        return
    }
    let update = update.unwrap();

    let final_status = updater::execute_update(update);
    status::write_status(args.output.clone(), final_status).expect("Unable to write status!");

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

    return Ok(Args {
        input,
        output
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
    output: String
}
