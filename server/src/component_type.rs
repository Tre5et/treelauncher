use std::fmt;
use std::str::FromStr;
use serde::{Deserialize, Serialize};

#[derive(Clone, Copy, PartialEq, Serialize, Deserialize, Debug)]
pub enum ComponentType {
    INSTANCE,
    SAVES,
    RESOURCEPACKS,
    OPTIONS,
    MODS,
    NONE
}

impl FromStr for ComponentType {
    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "instance" => Ok(ComponentType::INSTANCE),
            "saves" => Ok(ComponentType::SAVES),
            "resourcepacks" => Ok(ComponentType::RESOURCEPACKS),
            "options" => Ok(ComponentType::OPTIONS),
            "mods" => Ok(ComponentType::MODS),
            _ => Err(())
        }
    }
}

impl fmt::Display for ComponentType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", format!("{:?}", self).to_lowercase())
    }
}