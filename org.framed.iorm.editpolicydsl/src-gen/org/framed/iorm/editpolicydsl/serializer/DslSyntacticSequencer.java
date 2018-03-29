/*
 * generated by Xtext 2.13.0
 */
package org.framed.iorm.editpolicydsl.serializer;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynNavigable;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;
import org.framed.iorm.editpolicydsl.services.DslGrammarAccess;

@SuppressWarnings("all")
public class DslSyntacticSequencer extends AbstractSyntacticSequencer {

	protected DslGrammarAccess grammarAccess;
	protected AbstractElementAlias match_PrimaryConstraint_LeftParenthesisKeyword_1_0_a;
	protected AbstractElementAlias match_PrimaryConstraint_LeftParenthesisKeyword_1_0_p;
	protected AbstractElementAlias match_PrimaryFeature_LeftParenthesisKeyword_1_0_a;
	protected AbstractElementAlias match_PrimaryFeature_LeftParenthesisKeyword_1_0_p;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (DslGrammarAccess) access;
		match_PrimaryConstraint_LeftParenthesisKeyword_1_0_a = new TokenAlias(true, true, grammarAccess.getPrimaryConstraintAccess().getLeftParenthesisKeyword_1_0());
		match_PrimaryConstraint_LeftParenthesisKeyword_1_0_p = new TokenAlias(true, false, grammarAccess.getPrimaryConstraintAccess().getLeftParenthesisKeyword_1_0());
		match_PrimaryFeature_LeftParenthesisKeyword_1_0_a = new TokenAlias(true, true, grammarAccess.getPrimaryFeatureAccess().getLeftParenthesisKeyword_1_0());
		match_PrimaryFeature_LeftParenthesisKeyword_1_0_p = new TokenAlias(true, false, grammarAccess.getPrimaryFeatureAccess().getLeftParenthesisKeyword_1_0());
	}
	
	@Override
	protected String getUnassignedRuleCallToken(EObject semanticObject, RuleCall ruleCall, INode node) {
		return "";
	}
	
	
	@Override
	protected void emitUnassignedTokens(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		if (transition.getAmbiguousSyntaxes().isEmpty()) return;
		List<INode> transitionNodes = collectNodes(fromNode, toNode);
		for (AbstractElementAlias syntax : transition.getAmbiguousSyntaxes()) {
			List<INode> syntaxNodes = getNodesFor(transitionNodes, syntax);
			if (match_PrimaryConstraint_LeftParenthesisKeyword_1_0_a.equals(syntax))
				emit_PrimaryConstraint_LeftParenthesisKeyword_1_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if (match_PrimaryConstraint_LeftParenthesisKeyword_1_0_p.equals(syntax))
				emit_PrimaryConstraint_LeftParenthesisKeyword_1_0_p(semanticObject, getLastNavigableState(), syntaxNodes);
			else if (match_PrimaryFeature_LeftParenthesisKeyword_1_0_a.equals(syntax))
				emit_PrimaryFeature_LeftParenthesisKeyword_1_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if (match_PrimaryFeature_LeftParenthesisKeyword_1_0_p.equals(syntax))
				emit_PrimaryFeature_LeftParenthesisKeyword_1_0_p(semanticObject, getLastNavigableState(), syntaxNodes);
			else acceptNodes(getLastNavigableState(), syntaxNodes);
		}
	}

	/**
	 * Ambiguous syntax:
	 *     '('*
	 *
	 * This ambiguous syntax occurs at:
	 *     (rule start) (ambiguity) '!' rule=PrimaryConstraint
	 *     (rule start) (ambiguity) 'Contains' '(' type=FeatureTypeEnum
	 *     (rule start) (ambiguity) 'IsParent' '(' type=FeatureTypeEnum
	 *     (rule start) (ambiguity) 'IsSourceType' '(' type=FeatureTypeEnum
	 *     (rule start) (ambiguity) 'IsStepIn' '(' ')' (rule start)
	 *     (rule start) (ambiguity) 'IsTarget' '(' type=FeatureTypeEnum
	 *     (rule start) (ambiguity) 'IsTargetType' '(' type=FeatureTypeEnum
	 *     (rule start) (ambiguity) 'SourceEqualsTarget' '(' ')' (rule start)
	 *     (rule start) (ambiguity) 'SourceEqualsTargetType' '(' ')' (rule start)
	 *     (rule start) (ambiguity) 'false' (rule start)
	 *     (rule start) (ambiguity) 'true' (rule start)
	 *     (rule start) (ambiguity) {AndConstraintRule.rules+=}
	 *     (rule start) (ambiguity) {ImplicationConstraintRule.leftRule=}
	 *     (rule start) (ambiguity) {OrConstraintRule.rules+=}
	 */
	protected void emit_PrimaryConstraint_LeftParenthesisKeyword_1_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Ambiguous syntax:
	 *     '('+
	 *
	 * This ambiguous syntax occurs at:
	 *     (rule start) (ambiguity) '!' rule=PrimaryConstraint
	 *     (rule start) (ambiguity) {AndConstraintRule.rules+=}
	 *     (rule start) (ambiguity) {ImplicationConstraintRule.leftRule=}
	 *     (rule start) (ambiguity) {OrConstraintRule.rules+=}
	 */
	protected void emit_PrimaryConstraint_LeftParenthesisKeyword_1_0_p(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Ambiguous syntax:
	 *     '('*
	 *
	 * This ambiguous syntax occurs at:
	 *     (rule start) (ambiguity) '!' rule=PrimaryFeature
	 *     (rule start) (ambiguity) 'false' (rule start)
	 *     (rule start) (ambiguity) 'true' (rule start)
	 *     (rule start) (ambiguity) featureName=FeatureNameEnum
	 *     (rule start) (ambiguity) {AndFeatureRule.rules+=}
	 *     (rule start) (ambiguity) {OrFeatureRule.rules+=}
	 */
	protected void emit_PrimaryFeature_LeftParenthesisKeyword_1_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Ambiguous syntax:
	 *     '('+
	 *
	 * This ambiguous syntax occurs at:
	 *     (rule start) (ambiguity) '!' rule=PrimaryFeature
	 *     (rule start) (ambiguity) {AndFeatureRule.rules+=}
	 *     (rule start) (ambiguity) {OrFeatureRule.rules+=}
	 */
	protected void emit_PrimaryFeature_LeftParenthesisKeyword_1_0_p(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
}
