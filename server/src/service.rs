use std::str::FromStr;
use actix_web::{get, HttpResponse, HttpRequest};
use actix_web::http::header::ContentType;
use serde::{Serialize, Deserialize};

#[derive(Clone, Copy, PartialEq, Serialize, Deserialize)]
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
            "INSTANCE" => Ok(ComponentType::INSTANCE),
            "SAVES" => Ok(ComponentType::SAVES),
            "RESOURCEPACKS" => Ok(ComponentType::RESOURCEPACKS),
            "OPTIONS" => Ok(ComponentType::OPTIONS),
            "MODS" => Ok(ComponentType::MODS),
            _ => Err(())
        }
    }
}

#[derive(Serialize, Deserialize)]
pub struct ComponentResponse {
    component_type: ComponentType,
    id: String
}

#[get("/complete/{component_type}/{id}")]
pub async fn complete(req: HttpRequest) -> HttpResponse {
    let id = req.match_info().query("id").parse::<String>().unwrap_or("".to_string());
    let component_type = get_component_type(req).await;
    if component_type.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid component type: {}", component_type.err().unwrap()));
    }
    let component_type = component_type.unwrap();
    return HttpResponse::Ok()
        .content_type(ContentType::json())
        .json(ComponentResponse {
            component_type,
            id: id
        })
}

async fn get_component_type(req: HttpRequest) -> Result<ComponentType, String> {
    let component_type = req.match_info().query("component_type");
    let new_component_type = component_type.parse::<ComponentType>().unwrap_or(ComponentType::NONE);
    if new_component_type == ComponentType::NONE {
        return Err(component_type.to_string());
    }
    return Ok(new_component_type);
}