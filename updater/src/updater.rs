use crate::status::UpdaterStatus;
use crate::update::UpdateChange;

pub fn execute_update(update: Vec<UpdateChange>) -> UpdaterStatus {
    //TODO: Implement update execution

    return UpdaterStatus {
        status: crate::status::Status::SUCCESS,
        message: None,
        exceptions: None
    }
}