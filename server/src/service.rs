use std::fs;
use bytes::Bytes;
use std::path::Path;
use actix_web::{get, HttpResponse, HttpRequest, post};
use actix_web::http::header::ContentType;
use serde::{Serialize, Deserialize};
use crate::component_type::ComponentType;
use crate::get_auth_key;

#[derive(Serialize, Deserialize)]
pub struct FileData {
    path: String,
    content: String
}

#[derive(Serialize, Deserialize)]
pub struct GetResponse {
    version: u32,
    difference: Vec<String>
}

#[derive(Serialize, Deserialize, Clone)]
pub struct ComponentVersion {
    version: u32,
    difference: Vec<String>
}

#[derive(Serialize, Deserialize, Clone)]
pub struct ComponentDetails {
    versions: Vec<ComponentVersion>
}

#[derive(Serialize, Deserialize)]
pub struct Component {
    id: String,
    name: String
}

macro_rules! check_prerequisites {
    ($req:expr) => {
        check_prerequisites($req, true, true, None)
    };
    ($req:expr, $check_existing:expr) => {
        check_prerequisites($req, $check_existing, true, None)
    };
    ($req:expr, $check_existing:expr, $check_id:expr) => {
        check_prerequisites($req, $check_existing, $check_id, None)
    };
    ($req:expr, $check_existing:expr, $check_id:expr, $content_type:expr) => {
        check_prerequisites($req, $check_existing, $check_id, Some($content_type))
    };
}

#[get("/test")]
pub async fn test(req: HttpRequest) -> HttpResponse {
    let key_header = req.headers().get("auth-key");
    if key_header.is_none() || key_header.unwrap() != &get_auth_key() {
        return HttpResponse::Forbidden().finish();
    }
    return HttpResponse::Ok().finish();
}

#[get("/list/{component_type}")]
pub async fn list(req: HttpRequest) -> HttpResponse {
    let result = check_prerequisites!(req, false, false);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let path: &Path = Path::new(base_path.as_str());
    let children = path.read_dir();
    if children.is_err() {
        return HttpResponse::Ok()
            .content_type(ContentType::json())
            .json(Vec::<u8>::new());
    }
    let children = children.unwrap();
    let mut components: Vec<Component> = Vec::new();
    for child in children {
        if child.is_err() {
            continue;
        }
        let child = child.unwrap();
        let child_path = child.path();
        let component_files = child_path.read_dir();
        if component_files.is_err() {
            components.push(Component {
                id: child.file_name().into_string().unwrap(),
                name: "".to_string()
            });
            continue;
        }
        let component_files = component_files.unwrap();
        let mut found = false;
        for file in component_files {
            if file.is_err() {
                continue;
            }
            let file = file.unwrap();
            if file.file_name() != "manifest.json" {
                continue;
            }
            let content = fs::read_to_string(file.path());
            if content.is_err() {
                break;
            }
            let content = content.unwrap();
            if content.contains("\"name\":") {
                let string_after = content.split("\"name\":").collect::<Vec<&str>>()[1];
                let name = string_after.split("\"").collect::<Vec<&str>>()[1];
                components.push(Component {
                    id: child.file_name().into_string().unwrap(),
                    name: name.to_string()
                });
                found = true;
                break;
            }

        }
        if !found {
            components.push(Component {
                id: child.file_name().into_string().unwrap(),
                name: "".to_string()
            });
        }
    }
    return HttpResponse::Ok()
        .content_type(ContentType::json())
        .json(components);
}

#[get("/new/{component_type}/{id}")]
pub async fn new(req: HttpRequest) -> HttpResponse {
    let result = check_prerequisites!(req, false);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let details_path: String = format!("{p}/details.sync", p = base_path);
    let file: &Path = Path::new(details_path.as_str());
    if file.is_file() {
        return HttpResponse::BadRequest()
            .body(format!("Component already exists!"));
    }
    let content = ComponentDetails {
        versions: [
            ComponentVersion {
                version: 0,
                difference: Vec::new()
            }
        ].to_vec()
    };

    let prefix = file.parent().unwrap();
    let result = fs::create_dir_all(prefix);
    if result.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to create directories! Error: {}", result.err().unwrap()));
    }
    let result = fs::File::create(file);
    if result.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to create file! Error: {}", result.err().unwrap()));
    }
    let result = fs::write(details_path, serde_json::to_string(&content).unwrap());
    if result.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to write file! Error: {}", result.err().unwrap()));
    }

    return HttpResponse::Created().finish();
}

#[get("/get/{component_type}/{id}/{version}")]
pub async fn get(req: HttpRequest) -> HttpResponse {
    let version = req.match_info().query("version").parse::<u32>();
    let result = check_prerequisites!(req, false);
    if result.is_err() {
        return result.err().unwrap();
    }
    if version.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid version!"));
    }
    let version = version.unwrap();
    let base_path = result.unwrap();

    let details = get_details(base_path);
    if details.is_err() {
        return details.err().unwrap();
    }
    let details = details.unwrap();

    let version_exists = details.versions.iter().any(|v| v.version == version);
    if !version_exists {
        return HttpResponse::BadRequest()
            .body(format!("Invalid version!"));
    }
    let mut difference: Vec<String> = Vec::new();
    for v in details.versions.iter() {
        if v.version < version {
            continue;
        }
        for d in v.difference.iter() {
            if difference.iter().any(|e| d == e) {
                continue;
            }
            difference.push(d.clone());
        }
    }
    return HttpResponse::Ok()
        .content_type(ContentType::json())
        .json(GetResponse {
            version: details.versions.get(details.versions.len() - 1).unwrap().version,
            difference
        });
}

#[get("/complete/{component_type}/{id}")]
pub async fn complete(req: HttpRequest) -> HttpResponse {
    let result = check_prerequisites!(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let details_path: String = format!("{p}/details.sync", p = base_path);
    let details = get_details(base_path);
    if details.is_err() {
        return details.err().unwrap();
    }
    let details = details.unwrap();

    let version = details.versions.get(details.versions.len() - 1).unwrap().version + 1;

    let mut versions = details.versions.clone();
    versions.push(ComponentVersion {
        version,
        difference: Vec::new()
    });
    let content = ComponentDetails {
        versions
    };

    let result = fs::write(details_path, serde_json::to_string(&content).unwrap());
    if result.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to write details file! Error: {}", result.err().unwrap()));
    }

    return HttpResponse::Created().body(format!("{v}", v = version));
}

#[get("/file/{component_type}/{id}/{file_path:.*}")]
pub async fn get_file(req: HttpRequest) -> HttpResponse {
    let file_path = req.match_info().query("file_path").parse::<String>().unwrap_or("".to_string());
    let result = check_prerequisites!(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let path = format!("{b}/{p}", b = base_path, p = file_path);
    let file: &Path = Path::new(path.as_str());
    if !file.is_file() {
        return HttpResponse::NoContent().body("File doesn't exist!");
    }
    let read_file = fs::read(file);
    if read_file.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to read file! Error: {}", read_file.err().unwrap()));
    }
    let content = read_file.unwrap();
    return HttpResponse::Ok()
        .content_type(ContentType::octet_stream())
        .body(content);
}

#[post("file/{component_type}/{id}/{file_path:.*}")]
pub async fn post_file(req: HttpRequest, body: Bytes) -> HttpResponse {
    let file_path = req.match_info().query("file_path").parse::<String>();
    if file_path.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid file path!"));
    }
    let file_path = file_path.unwrap();
    let result = check_prerequisites!(req, true, true, "application/octet-stream");
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let details_path: String = format!("{p}/details.sync", p = base_path);

    let path = format!("{b}/{p}", b = base_path, p = file_path.as_str());
    let file: &Path = Path::new(path.as_str());
    if body.is_empty() {
        if !file.is_file() {
            return HttpResponse::Created().finish();
        }
        let result = fs::remove_file(file);
        let mut parent = file.parent().unwrap();
        while parent.read_dir().unwrap().next().is_none() {
            let result = fs::remove_dir(parent);
            if result.is_err() {
                return HttpResponse::InternalServerError()
                    .body(format!("Unable to remove parent directory! Error: {}", result.err().unwrap()));
            }
            parent = parent.parent().unwrap();
        }
        if result.is_err() {
            return HttpResponse::InternalServerError()
                .body(format!("Unable to remove file! Error: {}", result.err().unwrap()));
        }
    } else {
        if !file.is_file() {
            let prefix = file.parent().unwrap();
            let result = fs::create_dir_all(prefix);
            if result.is_err() {
                return HttpResponse::InternalServerError()
                    .body(format!("Unable to create parent directories! Error: {}", result.err().unwrap()));
            }
            let result = fs::File::create(file);
            if result.is_err() {
                return HttpResponse::InternalServerError()
                    .body(format!("Unable to create file! Error: {}", result.err().unwrap()));
            }
        }
        let result = fs::write(file, body);
        if result.is_err() {
            return HttpResponse::InternalServerError()
                .body(format!("Unable to write file! Error: {}", result.err().unwrap()));
        }
    }

    let details = get_details(base_path);
    if details.is_err() {
        return details.err().unwrap();
    }
    let details = details.unwrap();
    let version = details.versions.get(details.versions.len() - 1).unwrap();
    let mut difference = version.difference.clone();
    if !difference.iter().any(|e| &file_path == e) {
        difference.push(file_path);

        let new_version = ComponentVersion {
            version: version.version,
            difference
        };

        let mut versions = details.versions.clone();
        versions.remove(versions.len() - 1);
        versions.push(new_version);
        let content = ComponentDetails {
            versions
        };

        let result = fs::write(details_path, serde_json::to_string(&content).unwrap());
        if result.is_err() {
            return HttpResponse::InternalServerError()
                .body(format!("Unable to write version file! Error: {}", result.err().unwrap()));
        }
    }

    return HttpResponse::Created().finish();
}

#[post("/hash/{component_type}/{id}")]
pub async fn hash(req: HttpRequest, body: String) -> HttpResponse {
    let result = check_prerequisites!(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();

    let hash_request = serde_json::from_str(body.as_str());
    if hash_request.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid hash request! Error: {}", hash_request.err().unwrap()));
    }
    let hash_request: Vec<String> = hash_request.unwrap();

    let mut response: Vec<String> = Vec::new();
    for path in hash_request.iter() {
        let path = format!("{b}/{p}", b = base_path, p = path);
        let file: &Path = Path::new(path.as_str());
        if !file.is_file() {
            response.push("".to_string());
            continue;
        }
        let result = fs::read(file);
        if result.is_err() {
            response.push("".to_string());
            continue;
        }
        let result = result.unwrap();
        response.push(sha256::digest(&result));
    }

    return HttpResponse::Ok()
        .content_type(ContentType::json())
        .json(response);
}

pub fn check_prerequisites(req: HttpRequest, check_existing: bool, check_id: bool, content_type: Option<&str>) -> Result<String, HttpResponse> {
    let key_header = req.headers().get("auth-key");
    if key_header.is_none() || key_header.unwrap() != &get_auth_key() {
        return Err(HttpResponse::Forbidden().finish());
    }

    if content_type.is_some() {
        let content_type = content_type.unwrap();
        let content_type_header = req.headers().get("content-type");
        if content_type_header.is_none() || content_type_header.unwrap() != content_type {
            return Err(HttpResponse::BadRequest()
                .body(format!("Invalid content type! Please use {c}", c = content_type)));
        }
    }
    let component_type = req.match_info().query("component_type").parse::<ComponentType>();
    if component_type.is_err() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Invalid component type!")));
    }
    if !check_id {
        return Ok(format!("data/{t}", t = component_type.unwrap()));
    }
    let id = req.match_info().query("id").parse::<String>();
    if id.is_err() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Invalid id!")));
    }
    let id = id.unwrap();
    let path: String = format!("data/{t}/{i}", t = component_type.unwrap(), i = id);
    if !check_existing {
        return Ok(path);
    }
    let details_path: String = format!("{p}/details.sync", p = path);
    let file: &Path = Path::new(details_path.as_str());
    if !file.is_file() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Component doesn't exist! Try /new/{t}/{i} to create it.", t = component_type.unwrap(), i = id)));
    }
    return Ok(path);
}

pub fn get_details(base_path: String) -> Result<ComponentDetails, HttpResponse> {
    let path = format!("{b}/details.sync", b = base_path);
    let file: &Path = Path::new(path.as_str());
    if !file.is_file() {
        return Err(HttpResponse::InternalServerError()
            .body(format!("Unable to find details file!")));
    }
    let versions = fs::read_to_string(file);
    if versions.is_err() {
        return Err(HttpResponse::InternalServerError()
            .body(format!("Unable to read details file! Error: {e}", e = versions.err().unwrap())));
    }
    let versions= serde_json::from_str(&versions.unwrap());
    if versions.is_err() {
        return Err(HttpResponse::InternalServerError()
            .body(format!("Unable to parse version file! Error: {e}", e = versions.err().unwrap())));
    }
    return Ok(versions.unwrap());
}