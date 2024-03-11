use std::fs;
use std::path::Path;
use actix_web::{get, HttpRequest, HttpResponse};
use actix_web::http::header::ContentType;
use urlencoding::decode;
use semver::{Version, VersionReq};
use crate::get_local_item;
use crate::update_manifest::{ChangeElement, UpdateManifest, UpdateMode, UpdateResult};

#[get("/test")]
pub async fn test() -> HttpResponse {
    return HttpResponse::Ok().body("OK");
}

#[get("/update/{version}")]
pub async fn update(req: HttpRequest) -> HttpResponse {
    return get_update(req);
}

#[get("/update/{version}/{locale}")]
pub async fn update_locale(req: HttpRequest) -> HttpResponse {
    return get_update(req);
}

pub fn get_update(req: HttpRequest) -> HttpResponse {
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
    let locale = req.match_info().get("locale");
    let locale = if locale.is_some() { Some(locale.unwrap().to_string()) } else { None };

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
        if !found && version_str == input_version.to_string() {
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
            for c in v.changes.unwrap_or_else(|| Vec::new()) {
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
                let new_message = get_local_item(v.message.unwrap(), locale.clone());
                if message.is_empty() {
                    message = new_message;
                }
                else {
                    message = format!("{}\n{}", message, new_message);
                }
            }
        }
    }
    if !found {
        return HttpResponse::BadRequest().body("Version not found!");
    }
    if version_id.is_empty() {
        return HttpResponse::Ok().json(
            UpdateResult {
                id: None,
                changes: None,
                message: None,
                latest: Some(latest)
            }
        );
    }

    return HttpResponse::Ok().json(
        UpdateResult {
            id: Some(version_id),
            changes: Some(changes),
            message: if message.is_empty() { None } else { Some(message) },
            latest: Some(latest)
        }
    );
}

#[get("/file/{version}/{path:.*}")]
pub async fn file(req: HttpRequest) -> HttpResponse {
    let version = req.match_info().get("version");
    if version.is_none() {
        return HttpResponse::BadRequest().body("Version not specified!");
    }
    let version = version.unwrap();

    let path = req.match_info().get("path");
    if path.is_none() {
        return HttpResponse::BadRequest().body("Path not specified!");
    }
    let path = path.unwrap();
    let path = decode(path);
    if path.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid file path!"));
    }
    let path = path.unwrap();
    let path = path.replace("\\", "/");

    let data_dir = crate::get_data_dir();

    let mut file_string = format!("{}/{}/{}", data_dir.as_str(), version, path);
    let mut file_path = Path::new(file_string.as_str());

    if !file_path.is_file() {
        file_string = format!("{}/latest/{}", data_dir.as_str(), path);
        file_path = Path::new(file_string.as_str());
    }

    if !file_path.is_file() {
        return HttpResponse::NotFound().body("File not found!");
    }

    let file_contents = fs::read(file_path);
    if file_contents.is_err() {
        return HttpResponse::InternalServerError().body("Unable to read file!");
    }
    let file_contents = file_contents.unwrap();

    return HttpResponse::Ok().content_type(ContentType::octet_stream()).body(file_contents);
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