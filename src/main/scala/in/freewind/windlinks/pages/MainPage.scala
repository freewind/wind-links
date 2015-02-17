package in.freewind.windlinks.pages

import com.xored.scalajs.react.util.{ClassName, TypedEventListeners}
import com.xored.scalajs.react.{TypedReactSpec, scalax}
import in.freewind.windlinks.pages.main.Links
import in.freewind.windlinks.{Keycode, Link, SampleData}

object MainPage extends TypedReactSpec with TypedEventListeners {

  private val RefSearch = "search"
  private val RefSearchResult = "search-result"
  private val RefHighlightItem = "search-highlight-item"
  private val HighlightClass = "highlight-search-item"

  case class State(keyword: Option[String] = None,
                   searchResults: Seq[Result] = Nil,
                   highlightSearchItem: Option[Result] = None)

  case class Props()

  override def getInitialState(self: This) = State()

  implicit class Closure(self: This) {

    import self._

    val onSearch = input.onChange(e => {
      val keyword = Option(e.target.value).map(_.trim).filterNot(_.isEmpty)
      val results = keyword.map(filterSearchResult).getOrElse(Nil)

      setState(state.copy(keyword = keyword,
        searchResults = results,
        highlightSearchItem = results.headOption
      ))
    })

    val onKeyUp = input.onKeyUp(e => {
      e.which match {
        case Keycode.Up => moveSelectedLink(-1)
        case Keycode.Down => moveSelectedLink(1)
        case Keycode.Enter => clickOnHighlightLink()
        case _ =>
      }
    })

    private def clickOnHighlightLink(): Unit = {
      refs(RefHighlightItem).getDOMNode().click()
    }

    private def moveSelectedLink(step: Int): Unit = {
      for {
        hl <- state.highlightSearchItem
        index = state.searchResults.indexOf(hl)
        total = state.searchResults.length
        newIndex = (index + step + total) % total
      } setState(state.copy(highlightSearchItem = Some(state.searchResults(newIndex))))
    }
  }

  override def componentDidMount(self: MainPage.This): Unit = {
    self.refs(RefSearch).getDOMNode().focus()
  }

  @scalax
  override def render(self: This) = {
    val searchResults = self.state.searchResults
    <div id="main-page">
      <input placeholder="Search" onChange={self.onSearch} onKeyUp={self.onKeyUp} ref={RefSearch}/>
      {
        self.state.keyword match {
          case Some(keyword) =>
            <ul ref={RefSearchResult}>
              {
                searchResults.map { case item =>
                  val isHighlight = Some(item) == self.state.highlightSearchItem
                  val className = ClassName(HighlightClass -> isHighlight)
                  val refHighlightLink = if (isHighlight) RefHighlightItem else ""
                  <li className={className}>
                    <a href={item.link.url} target="_blank" ref={refHighlightLink}>[{item.projectName}] {item.link.url} - {item.link.name}</a>
                  </li>
                }
              }
            </ul>
          case _ => Links(Links.Props(projects))
        }
      }
    </div>
  }

  private val projects = SampleData.projects

  case class Result(projectName: String, link: Link)

  private def filterSearchResult(keyword: String): Seq[Result] = {
    def matches(url: String, keyword: String): Boolean = {
      url.toLowerCase.contains(keyword.toLowerCase)
    }

    for {
      project <- projects
      link <- project.basicLinks ++: project.moreLinkGroups.flatMap(_.links)
      if matches(link.url, keyword)
    } yield Result(project.name, link)
  }

}
