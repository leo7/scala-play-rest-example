package controllers.user

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import services.user.UserServiceComponent
import domain.user.User

trait UserController extends Controller {
    self: UserServiceComponent =>
    
    implicit val userReads = (__ \ "email").read[String]
                                           .map(resource => UserResource(resource))
    
    def createUser = Action(parse.json) {request =>
        unmarshalUserResource(request, (resource: UserResource) => {
            val user = User(Option.empty,
                            resource.email)
            userService.createUser(user)
            Created
        })
    }
    
    def updateUser(id: Long) = Action(parse.json) {request =>
        unmarshalUserResource(request, (resource: UserResource) => {
            val user = User(Option(id),
                            resource.email)
            userService.updateUser(user)
            NoContent
        })
    }
    
    private def unmarshalUserResource(request: Request[JsValue],
                                      block: (UserResource) => Result): Result = {
        request.body.validate[UserResource].fold(
            valid = block,
            invalid = (e => {
                val error = e.mkString
                Logger.error(error)
                BadRequest(error)
            })
        )
    }

}

case class UserResource (val email: String)
