package bequiet.domain

object score:
  enum Lamp(val prio: Int): // bigger == better
    case NoPlay extends Lamp(0)
    case Failed extends Lamp(1)
    case AssistClear extends Lamp(2)
    case EasyClear extends Lamp(3)
    case NormalClear extends Lamp(4)
    case HardClear extends Lamp(5)
    case ExHardClear extends Lamp(6)
    case FullCombo extends Lamp(7)

  object Lamp:
    def fromInt(n: Int): Lamp =
      List(
        NoPlay,
        Failed,
        AssistClear,
        EasyClear,
        NormalClear,
        HardClear,
        ExHardClear,
        FullCombo
      )(n)
    def toInt(l: Lamp): Int = l.prio

  type ScoreId = Int
  case class Score(
      id: ScoreId,
      lamp: Lamp
  )

  object Score:
    given Conversion[Score, ScoreId] = _.id

  case class ScoreSummary(
      lamp: Lamp
  )
