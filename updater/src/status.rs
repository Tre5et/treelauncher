use std::fs;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct UpdaterStatus {
    pub status: Status,
    pub message: Option<String>,
    pub exceptions: Option<Vec<String>>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub enum Status {
    UPDATING,
    SUCCESS,
    WARNING,
    FAILURE,
    FATAL
}

pub fn write_status(file: String, status: UpdaterStatus) -> Result<(), String> {
    let result = fs::write(file, serde_json::to_string(&status).unwrap());
    if result.is_err() {
        return Err(format!("Unable to write status file!\n{}", result.unwrap_err()));
    }
    return Ok(());
}