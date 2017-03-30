package models.analysis

import shapeless.{TypeCase, Typeable}

import scala.collection.SetLike

import play.api.libs.json.{Json, OFormat}

/**
  * TrackAnalysisSet companion object which has to be used to instantiate the class.
  * It also includes a Json formatter.
  */
object TrackAnalysisSet extends WithJsonFormatter[TrackAnalysisSet] {

  def apply(s: TrackAnalysis*): TrackAnalysisSet = TrackAnalysisSet(s)
  def empty: TrackAnalysisSet                    = new TrackAnalysisSet()

  def unapply(arg: TrackAnalysisSet): Option[Iterable[TrackAnalysis]] = Some(arg.backend)
  def apply(trackAnalysisSet: Iterable[TrackAnalysis]): TrackAnalysisSet =
    trackAnalysisSet.foldLeft(TrackAnalysisSet.empty)(_ + _)

  override implicit val implicitModelFormat: OFormat[TrackAnalysisSet] = Json.format[TrackAnalysisSet]

}

/**
  * A Set of TrackAnalysis which contains only a unique analysis of the same type.
  *
  * @param backend Internal representation for the set of TrackAnalysis
  */
class TrackAnalysisSet private (private val backend: Set[TrackAnalysis] = Set.empty[TrackAnalysis])
    extends Set[TrackAnalysis] with SetLike[TrackAnalysis, TrackAnalysisSet] {

  /**
    * Return true if the set contains this specific TrackAnalysis.
    *
    * @param key The TrackAnalysis
    * @return True if the set contains this specific TrackAnalysis (not only the type of analysis). False otherwise
    */
  override def contains(key: TrackAnalysis): Boolean = {
    backend.contains(key)
  }

  /**
    * Return true if the set contains this type of analysis.
    *
    * @param name the name of the TrackAnalysis
    * @return True if it contains this type of analysis. False otherwise.
    */
  def contains(name: String): Boolean = {
    backend.exists(_.name == name)
  }

  /**
    * Return true if the set contains this type of analysis.
    *
    * @tparam A The type of the analysis
    * @return True if it contains this type of analysis. False otherwise.
    */
  def contains[A <: TrackAnalysis: Typeable]: Boolean = {
    val analysisType = TypeCase[A]
    backend.exists {
      case analysisType(a) => true
      case _               => false
    }
  }

  /**
    * Return the analysis of the given type of analysis if it's in the set.
    *
    * @tparam A The type of analysis we want.
    * @return Maybe the analysis of the type we asked.
    */
  def get[A <: TrackAnalysis: Typeable]: Option[A] = {
    val analysisType = TypeCase[A]
    backend.flatMap {
      case analysisType(a) => Some(a)
      case _               => None
    }.headOption
  }

  /**
    * Add the TrackAnalysis in the set. If there was already one of the same type. It overrides it.
    * If the trackAnalysis was already in the set it leaves the set unchanged.
    *
    * @param elem The track analysis to add.
    * @return A new TrackAnalysisSet including the new analysis.
    */
  override def +(elem: TrackAnalysis): TrackAnalysisSet = {
    if(contains(elem)){
      this
    } else {
      new TrackAnalysisSet(
        backend.filter(_.name != elem.name) + elem
      )
    }
  }

  /**
    * Remove the exact given TrackAnalysis from the set if there was any. Leave the set unchanged otherwise.
    * It won't remove the analysis if there is one of the same type but with different values.
    *
    * @param elem The track analysis to remove.
    * @return A new TrackAnalysisSet without the track analysis.
    */
  override def -(elem: TrackAnalysis) = {
    if (contains(elem)) {
      this
    } else {
      TrackAnalysisSet(backend - elem)
    }
  }

  /**
    * Remove this type of analysis from the set if it there was any. Leave the set unchanged otherwise.
    *
    * @tparam A Type of the analysis to remove from the set.
    * @return A new TrackAnalysisSet without the track analysis type.
    */
  def -[A <: TrackAnalysis: Typeable]: TrackAnalysisSet = {
    val analysisType = TypeCase[A]
    if(contains[A]){
      new TrackAnalysisSet(
        backend.filter {
          case analysisType(a) => false
          case _               => true
        }
      )
    } else {
      this
    }
  }

  /**
    * Remove this type of analysis from the set if it there was any. Leave the set unchanged otherwise.
    *
    * @param name The name of the type of analysis to remove.
    * @return A new TrackAnalysisSet without the track analysis type.
    */
  def -(name: String): TrackAnalysisSet = {
    if(contains(name)){
      new TrackAnalysisSet(
        backend.filter(_.name == name)
      )
    } else {
      this
    }
  }

  override def empty = TrackAnalysisSet.empty

  override def iterator: Iterator[TrackAnalysis] = backend.iterator

}
