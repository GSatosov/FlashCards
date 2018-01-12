package models.itemsAndDecks

import models.ids.Ids.{DeckId, UserId}

case class DecksInfoEntry(id: DeckId = DeckId(0),
                          name: String,
                          authorId: UserId,
                          basedOn: Option[DeckId])

