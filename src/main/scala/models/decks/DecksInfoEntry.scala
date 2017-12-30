package models.decks

import models.ids.Ids.{DeckId, UserId}

case class DecksInfoEntry(id: DeckId = DeckId(0),
                          authorId: UserId,
                          subscribers: List[Long])

