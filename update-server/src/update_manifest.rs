use std::ffi::c_int;
use serde::{Deserialize, Serialize};
use crate::LocalItem;

#[derive(Serialize, Deserialize, Debug)]
pub struct UpdateManifest {
    pub id: Option<String>,
    pub requires: Option<String>,
    pub changes: Option<Vec<ChangeElement>>,
    pub message: Option<Vec<LocalItem>>
}

#[derive(Serialize, Deserialize, Debug)]
pub struct UpdateResult {
    pub id: Option<String>,
    pub changes: Option<Vec<ChangeElement>>,
    pub message: Option<String>,
    pub latest: Option<bool>
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