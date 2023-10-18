use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct News {
    pub version: Option<String>,
    pub content: String
}