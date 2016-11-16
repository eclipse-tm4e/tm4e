package fr.opensagres.language.textmate.eclipse.samples.css;

import fr.opensagres.language.textmate.core.grammar.IGrammar;
import fr.opensagres.language.textmate.core.registry.Registry;
import fr.opensagres.language.textmate.eclipse.text.TMPresentationReconciler;
import fr.opensagres.language.textmate.eclipse.text.styles.CSSTokenProvider;
import fr.opensagres.language.textmate.eclipse.text.styles.ITokenProvider;

public class CSSPresentationReconcilier extends TMPresentationReconciler {

	public CSSPresentationReconcilier() {
		// Set the C# grammar
		super.setGrammar(initGrammar());
		// Set the token provider used to style editor tokens
		super.setTokenProvider(initTokenProvider());
	}

	private IGrammar initGrammar() {
		// TODO: cache the grammar
		Registry registry = new Registry();
		try {
			return registry.loadGrammarFromPathSync("css.plist",
					CSSPresentationReconcilier.class.getResourceAsStream("css.plist"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ITokenProvider initTokenProvider() {
		// TODO: cache the token provider
		return new CSSTokenProvider(CSSPresentationReconcilier.class.getResourceAsStream("style_Solarized-light.css"));
	}

}
