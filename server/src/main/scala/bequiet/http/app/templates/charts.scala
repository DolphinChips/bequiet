package bequiet.http.app.templates

import scalatags.Text.all.*
import bequiet.domain.chart.Chart
import bequiet.domain.song.Song

def charts(c: List[Chart], song: Song) = doctype("html")(
  html(lang := "en")(
    head(title := s"Charts for ${song.title}"),
    body(
      for chart <- c
      yield "lol no"
    )
  )
)
