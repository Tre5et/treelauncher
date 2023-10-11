use std::ffi::c_int;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct UpdateManifest {
    pub id: String,
    pub requires: Option<String>,
    pub changes: Vec<ChangeElement>,
    pub message: Option<String>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct ChangeElement {
    pub path: String,
    pub mode: UpdateMode,
    pub elements: Option<Vec<UpdateElement>>,
    pub updater: Option<bool>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct UpdateElement {
    pub pattern: Option<String>,
    pub value: String,
    pub meta: Option<c_int>,
    pub replace: Option<bool>
}

#[derive(Serialize, Deserialize, Clone, PartialEq, Debug)]
pub enum UpdateMode {
    FILE,
    DELETE,
    REGEX,
    LINE
}