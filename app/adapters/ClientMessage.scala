package adapters

import adapters.messages._
import play.api.libs.json.{JsValue, Json}

object ClientMessage {

  implicit def jsValue2ClientMessage(jsValue: JsValue): ClientMessage = {
    (jsValue \ "$type").as[String] match {
      case MessagePing.MSG_TYPE => jsValue.as[MessagePing]
      case MessageLogin.MSG_TYPE => jsValue.as[MessageLogin]

      case _ => ErrorMessage("UNKNOWN_MESSAGE_TYPE")
    }
  }

  implicit def clientMessage2jsValue(clientMessage: ClientMessage): JsValue = {
    clientMessage match {
      case msgPong: MessagePong => Json.toJson(msgPong)
      case msgLoginSucc: MessageLoginSuccessful => Json.toJson(msgLoginSucc)
      case msgLoginFail: MessageLoginFailed => Json.toJson(msgLoginFail)

      case msgError: ErrorMessage => Json.toJson(msgError)
    }
  }

}

class ClientMessage