package lv.rtu.itd.wiki.portlets;

import java.io.StringReader;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import lv.rtu.itd.wiki.portlets.dao.IWikiDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.wiki.WikiModel;

public class WikiController extends SimpleFormController {

	private static final Log log = LogFactory.getLog(WikiController.class);

	public static final String EDIT_ROLE = "wikiEditors";

	protected IWikiDao wikiDao;

	public IWikiDao getWikiDao() {
		return wikiDao;
	}

	public void setWikiDao(IWikiDao wikiDao) {
		this.wikiDao = wikiDao;
	}

	public WikiController() {
		super();
		this.setSynchronizeOnSession(true);
		this.setFormView("wiki/view");
		this.setSuccessView("wiki/view");
		this.setCommandClass(WikiObject.class);
		this.setCommandName("wiki");
	}

	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) {
		String portletId = getPageSpaceName(request);
		String wikiPageName = getCurrPageName(request);

		PortletURL renderUrl = response.createRenderURL();
		// hook, setting empty param
		renderUrl.setParameter(PortalWikiModel.PAGE_PARAM, "");

		// Home url
		PortletURL homeURL = response.createRenderURL();
		homeURL.setParameter(PortalWikiModel.PAGE_PARAM, PortalWikiModel.ROOT_PAGE);
		homeURL.setParameter(PortalWikiModel.ACTION_PARAM, PortalWikiModel.VIEW_ACTION);
		// homeURL.setParameter(PortalWikiModel.LANG_PARAM, getPortletLang(request));
		// Edit url
		PortletURL editURL = response.createRenderURL();
		editURL.setParameter(PortalWikiModel.PAGE_PARAM, wikiPageName);
		editURL.setParameter(PortalWikiModel.ACTION_PARAM, PortalWikiModel.EDIT_ACTION);
		editURL.setParameter(PortalWikiModel.LANG_PARAM, getPortletLang(request));
		// Edit url
		PortletURL listURL = response.createRenderURL();
		listURL.setParameter(PortalWikiModel.ACTION_PARAM, PortalWikiModel.LIST_ACTION);
		// listURL.setParameter(PortalWikiModel.LANG_PARAM, getPortletLang(request));

		String action = request.getParameter(PortalWikiModel.ACTION_PARAM);

		if (action == null) {
			action = PortalWikiModel.VIEW_ACTION;
		}
		boolean isEditor = request.isUserInRole(EDIT_ROLE);
		if (!isEditor) {
			if (action.equals(PortalWikiModel.EDIT_ACTION)) {
				action = PortalWikiModel.VIEW_ACTION;
			}
		}
		WikiObject wikiTmp;
		if (action.equals(PortalWikiModel.EDIT_ACTION)) {
			wikiTmp = wikiDao.getWikiObjectByNameForEdit(wikiPageName, portletId, getPortletLang(request));
			log.info("EDIT object get");
		} else {
			wikiTmp = wikiDao.getWikiObjectByName(wikiPageName, portletId, getPortletLang(request));
			log.info("VieW object get");

		}

		if (action.equals(PortalWikiModel.LIST_ACTION)) {
			ModelAndView mavList = new ModelAndView("wiki/list");
			List<WikiObject> wikiList = wikiDao.getWikiObjectsByPortletId(portletId, getPortletLangReal(request));
			mavList.addObject("wikiList", wikiList);

			mavList.addObject("urlBasePart", renderUrl.toString());
			mavList.addObject("homeUrl", homeURL.toString());
			return mavList;
		}
		if (action.equals(PortalWikiModel.EDIT_ACTION)) {
			if (wikiTmp == null) {
				wikiTmp = new WikiObject(wikiPageName, getPortletLang(request));
			}

			// Form return url
			log.info(request.getRemoteUser() + " trying to edit ");
			String returnURL = response.createActionURL().toString();
			ModelAndView mavEdit = new ModelAndView("wiki/edit");
			mavEdit.addObject("wiki", wikiTmp);
			mavEdit.addObject("returnUrl", returnURL);
			mavEdit.addObject("homeUrl", homeURL.toString());
			mavEdit.addObject("listUrl", listURL.toString());
			return mavEdit;
		}

		ModelAndView mav = new ModelAndView("wiki/view");
		boolean showLangs = showLangs(request, wikiTmp);
		List<LangObject> langAv = wikiDao.getAvialability(wikiPageName, portletId);

		mav.addObject("langAv", langAv);
		mav.addObject("showLangs", showLangs);
		if (wikiTmp != null) {
			log.info("TEXT:" + wikiTmp.getWikiText());

			String xwikihtml;
			try {
				xwikihtml = convertWikiToHtml(wikiTmp.getWikiText() == null ? "" : wikiTmp.getWikiText(), response,
				        request);
			} catch (ConversionException e) {
				xwikihtml = "Sorry, error occurred.";
				e.printStackTrace();
			} catch (ComponentLookupException e) {
				xwikihtml = "Sorry, error occurred.";
				e.printStackTrace();
			}

			mav.addObject("title", wikiTmp.getTitle());
			mav.addObject("xwikihtml", xwikihtml);
			mav.addObject("pagename", wikiTmp.getName());
			mav.addObject("currLang", wikiTmp.getLang());
		} else {
			mav.addObject("currLang", getPortletLang(request));
		}

		if (isEditor) {
			mav.addObject("editUrl", editURL.toString());
			mav.addObject("listUrl", listURL.toString());
		}
		mav.addObject("urlBasePart", renderUrl.toString());
		mav.addObject("LANG_PARAM", PortalWikiModel.LANG_PARAM);
		mav.addObject("ACTION_PARAM", PortalWikiModel.ACTION_PARAM);

		// if not in home, add button to home
		if (!wikiPageName.equals(PortalWikiModel.ROOT_PAGE)) {
			mav.addObject("homeUrl", homeURL.toString());
		}

		return mav;
	}

	@Override
	protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors)
	        throws Exception {
		String portletId = getPageSpaceName(request);
		WikiObject wo = (WikiObject) command;
		log.debug(wo.getName());
		log.debug(wo.getWikiText());
		wo.setPortletId(portletId);
		if (request.isUserInRole(EDIT_ROLE)) {
			WikiObject tmpArticle = wikiDao.getWikiObjectByName(wo.getName(), portletId, getPortletLang(request));
			if (tmpArticle == null) {
				wikiDao.addWikiObject(wo);
			} else {
				wo.setId(tmpArticle.getId());
				wikiDao.updateWikiObject(wo);
			}
		}
		response.setRenderParameter(PortalWikiModel.PAGE_PARAM, wo.getName());
		response.setRenderParameter(PortalWikiModel.LANG_PARAM, wo.getLang());
	}

	private String convertWikiToHtml(String wiki, RenderResponse response, RenderRequest request)
	        throws ConversionException, ComponentLookupException {
		PortletURL url = response.createRenderURL();
		try {
			url.setWindowState(WindowState.NORMAL);
		} catch (WindowStateException e) {
			e.printStackTrace();
		}
		// // hook, setting empty param
		// url.setParameter(PortalWikiModel.PAGE_PARAM, "");

		String portletId = getPageSpaceName(request);
		WikiModel wikiModel = new PortalWikiModel(url.toString(), wikiDao, portletId, getPortletLang(request));
		log.info("converting: started");

		DefaultComponentDescriptor<WikiModel> cd = new DefaultComponentDescriptor<WikiModel>();
		cd.setRole(WikiModel.class);

		// Initialize Rendering components and allow getting instances
		EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
		componentManager.initialize(this.getClass().getClassLoader());
		componentManager.registerComponent(cd, wikiModel);

		// Use the Converter component to convert between one syntax to another.
		Converter converter;
		converter = componentManager.lookup(Converter.class);

		// Convert input in XWiki Syntax 2.0 into XHTML. The result is stored in the printer.
		WikiPrinter printer = new DefaultWikiPrinter();

		converter.convert(new StringReader(wiki), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer);

		String xwikihtml = printer.toString();
		log.info("converting: finished");
		return xwikihtml;
	}

	/**
	 * Page space name is used for grouping wiki pages. This ensures that pages from different portlets with same names
	 * won't collide. And in the mean time setting 'pageSpace' parameter in portlet preferences to same values for
	 * different portlets, allows those portlets to share the page pool. But in this situation it is recommended to set
	 * 'rootPage' parameter for all but one of those portlets so that first page would be different for each of those
	 * portlets.
	 * 
	 * @param request
	 *            - portelt request.
	 * @return Portlet identifier which is used for page grouping.
	 */
	private String getPageSpaceName(PortletRequest request) {
		// uPortal specific portlet ID retrieval
		final String pageSpaceName = request.getWindowID().split("_")[0];

		// now get name from portlet preferences so that prefs could override default value
		final String overridenWindowId = request.getPreferences().getValue("pageSpaceName", pageSpaceName);
		log.info("Page space name: " + pageSpaceName);
		return overridenWindowId;
	}

	/**
	 * 'rootPage' portlet preference parameter sets the name of the first page to use. This parameter should be used
	 * whenever portlets share the same page space.
	 * 
	 * @param request
	 *            - portlet request
	 * @return Root page name.
	 */
	private String getPortletRootPageName(PortletRequest request) {
		String rootPage = request.getPreferences().getValue("rootPage", "ROOT");
		if (rootPage == null) {
			rootPage = "ROOT";
		}
		log.info("Root page: " + rootPage);
		return rootPage;
	}

	/**
	 * Get a locale to use for content. By default user preferred locale (provided by portal) will be used, but if
	 * 'lang' attribute is present among portlet request parameters, then this value will override user preferred
	 * locale. This mechanism allows content authors to switch between locales while common users will get a content in
	 * their preferred locale (if a content for that locale exists).
	 * 
	 * @param request
	 *            - portlet request.
	 * @return locale code for content to view.
	 */
	private String getPortletLang(PortletRequest request) {
		String lang = request.getParameter("lang");
		if (lang == null) {
			lang = request.getLocale().toString();
		}
		log.debug("Lang: " + lang);
		return lang;
	}

	private String getPortletLangReal(PortletRequest request) {
		String lang = request.getLocale().toString();

		log.debug("Real lang: " + lang);
		return lang;
	}

	private String getCurrPageName(PortletRequest request) {
		String wikiPageName = request.getParameter(PortalWikiModel.PAGE_PARAM);
		log.debug((wikiPageName == null) ? "empty page name" : wikiPageName);

		if (wikiPageName == null) {
			wikiPageName = getPortletRootPageName(request);
		}
		return wikiPageName;
	}

	private boolean isLangReal(PortletRequest request) {
		if (getPortletLang(request).equals(getPortletLangReal(request))) {
			log.info("LANGLANG real: " + getPortletLangReal(request));
			log.info("LANGLANG: " + getPortletLang(request));
			return true;
		}
		return false;
	}

	private boolean showLangs(PortletRequest request, WikiObject wikiTmp) {
		boolean showLangs = !isLangReal(request);
		boolean isEditor = request.isUserInRole(EDIT_ROLE);
		if (isEditor || wikiTmp == null) {
			showLangs = true;
		}
		return showLangs;

	}
}
