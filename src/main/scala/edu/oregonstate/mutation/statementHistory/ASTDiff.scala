package edu.oregonstate.mutation.statementHistory

import fr.labri.gumtree.actions.ActionGenerator
import fr.labri.gumtree.actions.model.Action
import fr.labri.gumtree.gen.jdt.JdtTreeGenerator
import fr.labri.gumtree.matchers.MatcherFactories
import fr.labri.gumtree.tree.Tree

/**
 * Created by caius on 9/24/15.
 */
class ASTDiff {
  def getActions(AContent:String, BContent:String): Seq[Action] = {
    var jdtTreeGenerator = new JdtTreeGenerator()
    var leftTree = getTree(AContent, jdtTreeGenerator)
    var rightTree = getTree(BContent, jdtTreeGenerator)
    getActions(leftTree, rightTree)
  }

  def getActions(leftTree:Tree, rightTree: Tree): Seq[Action] = {
    import scala.collection.JavaConversions._
    var matcher = MatcherFactories.newMatcher(leftTree, rightTree)
    matcher.`match`();
    var actionGenerator = new ActionGenerator(leftTree, rightTree, matcher.getMappings)
    actionGenerator.generate()
    asScalaBuffer(actionGenerator.getActions).toSeq
  }

  private def getTree(AContent: String, jdtTreeGenerator: JdtTreeGenerator): Tree = {
    jdtTreeGenerator.fromString(AContent);
  }

  def getTree(content: String): Tree =  {
    var jdtTreeGenerator = new JdtTreeGenerator();
    getTree(content, jdtTreeGenerator);
  }

}
