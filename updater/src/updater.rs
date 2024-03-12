use std::process::Command;
use crate::status::{Status, UpdaterStatus};
use crate::update::UpdateChange;

pub fn execute_update(update: Vec<UpdateChange>) -> UpdaterStatus {
    //TODO: Implement update execution

    return UpdaterStatus {
        status: Status::SUCCESS,
        message: None,
        exceptions: None
    }
}

pub fn restart(dir: String, command: String) {
    Command::new(command).current_dir(dir).spawn().expect("Failed to restart!");
}