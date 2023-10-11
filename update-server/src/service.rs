use std::fs;
use std::path::Path;
use actix_web::{get, HttpRequest, HttpResponse};
use semver::{Version, VersionReq};
use crate::update_manifest::{ChangeElement, UpdateManifest, UpdateMode};

#[get("/test")]
pub async fn test() -> HttpResponse {
    return HttpResponse::Ok().body("OK");
}

#[get("/update/{version}")]
pub async fn update(req: HttpRequest) -> HttpResponse {
    let input_version = req.match_info().get("version");
    if input_version.is_none() {
        return HttpResponse::BadRequest().body("Version not specified!");
    }
    let input_version = input_version.unwrap();
    let input_version = Version::parse(input_version);
    if input_version.is_err() {
        return HttpResponse::BadRequest().body("Invalid version!");
    }
    let input_version = input_version.unwrap();

    let version_file = read_version_file();
    if version_file.is_err() {
        return version_file.unwrap_err();
    }
    let version_file = version_file.unwrap();

    let mut found = false;
    let mut version_id = String::new();
    let mut changes: Vec<ChangeElement> = Vec::new();
    let mut message = String::new();
    let mut latest = false;
    for v in version_file {
        let version_str = v.id.unwrap();
        let version: VersionReq = VersionReq::parse(version_str.as_str()).unwrap();
        if !found && version.matches(&input_version) {
            found = true;
            latest = true;
        } else if found {
            let requires = v.requires;
            if requires.is_some() {
                let requires = VersionReq::parse(requires.unwrap().as_str()).unwrap();
                if !requires.matches(&input_version) {
                    latest = false;
                    continue;
                }
            }
            version_id = version_str;
            latest = true;
            for c in v.changes.unwrap() {
                let mut dealt_with = false;
                for o in changes.clone() {
                    let c_copy = c.clone();
                    if o.path == c.path {
                        if c.mode == UpdateMode::DELETE {
                            changes.retain(|x| x.path != c.path);
                        }
                        if o.mode != UpdateMode::FILE || c.mode != UpdateMode::FILE {
                            changes.push(c_copy);
                        }
                        dealt_with = true;
                        break;
                    }
                }
                if !dealt_with {
                    changes.push(c);
                }
            }
            if v.message.is_some() {
                let new_line = v.message.unwrap().replace("{v}", version_id.as_str());
                message = format!("{}\n{}", message, new_line);
            }
        }
    }
    if !found {
        return HttpResponse::BadRequest().body("Version not found!");
    }
    if version_id.is_empty() {
        return HttpResponse::Ok().json(
            UpdateManifest {
                id: None,
                requires: None,
                changes: None,
                message: None,
                latest: Some(latest)
            }
        );
    }

    return HttpResponse::Ok().json(
        UpdateManifest {
            id: Some(version_id),
            requires: None,
            changes: Some(changes),
            message: if message.is_empty() { None } else { Some(message) },
            latest: Some(latest)
        }
    );
}

fn read_version_file() -> Result<Vec<UpdateManifest>, HttpResponse> {
    let data_dir = crate::get_data_dir();
    let version_file = format!("{}/versions.json", data_dir.as_str());
    let path = Path::new(version_file.as_str());
    if !path.is_file() {
        return Err(HttpResponse::InternalServerError().body("Versions file not found!"));
    }

    let versions_contents = fs::read_to_string(path);
    if versions_contents.is_err() {
        return Err(HttpResponse::InternalServerError().body("Unable to read versions file!"));
    }
    let versions_contents = versions_contents.unwrap();

    let versions: Result<Vec<UpdateManifest>,_> = serde_json::from_str(versions_contents.as_str());
    if versions.is_err() {
        return Err(HttpResponse::InternalServerError().body("Unable to parse versions file!"));
    }
    return Ok(versions.unwrap());
}