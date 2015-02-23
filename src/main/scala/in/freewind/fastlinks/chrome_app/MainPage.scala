package in.freewind.fastlinks.chrome_app

import com.xored.scalajs.react.util.TypedEventListeners
import com.xored.scalajs.react.{TypedReactSpec, scalax}
import in.freewind.fastlinks._
import in.freewind.fastlinks.chrome_app.main.Header
import in.freewind.fastlinks.chrome_extension.FastLinks

object MainPage extends TypedReactSpec with TypedEventListeners {

  case class State(currentCategory: Option[Category] = None, currentProject: Option[Project] = None)

  case class Props(meta: Option[Meta] = None, appBackend: AppBackend)

  override def getInitialState(self: This) = State()

  override def componentWillReceiveProps(self: This, nextProps: Props): Unit = {
    nextProps.meta.map { meta =>
      val currentCategory = meta.categories.headOption
      val currentProject = currentCategory.flatMap(_.projects.headOption)
      self.setState(self.state.copy(currentCategory = currentCategory, currentProject = currentProject))
    }
  }

  implicit class Closure(self: This) {

    import self._

    def selectCategory(category: Category): Unit = {
      setState(state.copy(currentCategory = Some(category)))
    }
  }

  @scalax
  override def render(self: This) = {
    <div>
      { Header(Header.Props(self.props.meta, self.selectCategory, self.props.appBackend)) }
      <div>
        {
          FastLinks(FastLinks.Props(projects = self.state.currentCategory.map(_.projects).getOrElse(Nil)))
        }
      </div>
    </div>
  }

}
