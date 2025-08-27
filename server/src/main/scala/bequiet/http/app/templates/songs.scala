package bequiet.http.app.templates

import scalatags.Text.all.{title => _, *}
import scalatags.Text.tags2.title
import bequiet.domain.song.Song

def songs(s: List[Song]) = doctype("html")(
  html(lang := "en")(
    head()(title("Songs")),
    body(
      table(
        thead(tr(th("Song"), th())),
        tbody(
          for song <- s
          yield tr(
            td(song.title),
            td(a(href := s"/song/${song.id}/charts")("Charts"))
          )
        )
      )
    )
  )
)
