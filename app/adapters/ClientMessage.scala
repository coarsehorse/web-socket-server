package adapters

import adapters.messages._
import play.api.libs.json._

object ClientMessage {
  /**
    * Convert JsValue to ClientMessage
    * @param jsValue Clients JSON
    * @return ClientMessage representation of JSON
    */
  implicit def jsValue2ClientMessage(jsValue: JsValue): ClientMessage = {
    (jsValue \ "$type") match {
      case jsDef: JsDefined =>
        jsDef.as[String] match {
          // Messages from clients
          case MessagePing.MSG_TYPE         => jsValue.as[MessagePing]
          case MessageLogin.MSG_TYPE        => jsValue.as[MessageLogin]
          case MessageSubscribe.MSG_TYPE    => jsValue.as[MessageSubscribe]
          case MessageUnSubscribe.MSG_TYPE  => jsValue.as[MessageUnSubscribe]

          // Messages from admins
          case MessageAddTable.MSG_TYPE     => jsValue.as[MessageAddTable]
          case MessageUpdateTable.MSG_TYPE  => jsValue.as[MessageUpdateTable]
          case MessageRemoveTable.MSG_TYPE  => jsValue.as[MessageRemoveTable]

          // Messages replied from subscription actor
          case MessageTableAdded.MSG_TYPE   => jsValue.as[MessageTableAdded]
          case MessageTableUpdated.MSG_TYPE => jsValue.as[MessageTableUpdated]
          case MessageTableRemoved.MSG_TYPE => jsValue.as[MessageTableRemoved]

          // Messages with strange $type
          case _                            => MessageError("UNKNOWN_MESSAGE_TYPE")
        }
      case jsUnd: JsUndefined               => MessageError("UNKNOWN_MESSAGE_TYPE")
    }
  }

  /**
    * Convert ClientMessage to JsValue
    * @param clientMessage ClientMessage that needs to be converted
    * @return JsValue representation of message
    */
  implicit def clientMessage2jsValue(clientMessage: ClientMessage): JsValue = {
    clientMessage match {
      case msgPong:       MessagePong               => Json.toJson(msgPong)
      case msgLoginSucc:  MessageLoginSuccessful    => Json.toJson(msgLoginSucc)
      case msgLoginFail:  MessageLoginFailed        => Json.toJson(msgLoginFail)
      case msgTableList:  MessageTableList          => Json.toJson(msgTableList)
      case msgUnSubDone:  MessageUnSubscribeDone    => Json.toJson(msgUnSubDone)
      case msgAdTFailed:  MessageAddTableFailed     => Json.toJson(msgAdTFailed)
      case msgTablAdded:  MessageTableAdded         => Json.toJson(msgTablAdded)
      case msgTablUpded:  MessageTableUpdated       => Json.toJson(msgTablUpded)
      case msgUpTFailed:  MessageUpdateTableFailed  => Json.toJson(msgUpTFailed)
      case msgReTFailed:  MessageRemoveTableFailed  => Json.toJson(msgReTFailed)
      case msgTableRmvd:  MessageTableRemoved       => Json.toJson(msgTableRmvd)

      case msgError:      MessageError              => Json.toJson(msgError)
    }
  }
}

class ClientMessage
