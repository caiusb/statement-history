package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.SuperTree
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.jdt.JavaTree

/**
 * Created by caius on 2/11/16.
 */
class StatementInfoTest extends GitTest {

  private implicit def st(s: SuperTree): ASTNode = s.asInstanceOf[JavaTree]

  private val FILE_NAME = "A.java"

  def makeClass(block: String): String = {
    "public class A{\npublic void m(){\n" + block + "\n{\nint x=3;}}}"
  }

  def find(commit: RevCommit): SuperTree = {
    BlockFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 4)
  }

  it should "have the correct info for a if block" in {
    val node = find(add(FILE_NAME, makeClass("if(true)")))
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",4,if(4:5),Block,A,m,")
  }

  it should "have the correct info for a while block" in {
    val node = find(add(FILE_NAME, makeClass("while(true)")))
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",4,while(4:5),Block,A,m,")
  }

  it should "have the correct info for a for block" in {
    val node = find(add(FILE_NAME, makeClass("for(int i=0;i<10;i++)")))
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",4,for(4:5),Block,A,m,")
  }

  it should "have the correct info for an enhanced for block" in {
    val node = find(add(FILE_NAME, makeClass("for(String x:xes)")))
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",4,for(4:5),Block,A,m,")
  }

  it should "have the correct info for a do-while block" in {
    val commit = add(FILE_NAME, "public class A{\npublic void m(){\ndo {\nint x=3;\n}while(true);\n}\n}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 4)
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",3,do(3:5),Block,A,m,")
  }

  it should "have the correct info for a try block" in {
    val node = find(add(FILE_NAME, "public class A{\npublic void m(){\ntry\n{\nint x=3;}catch(Exception e){}}}"))
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",4,try(4:5),Block,A,m,")
  }

  it should "have the correct info for a catch block" in {
    val commit = add(FILE_NAME, "public class A{\npublic void m(){\ntry\n{\nint x=3;}catch(Exception e){\nint y=3;}}}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 5)
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",5,catch(5:6),Block,A,m,")
  }

  it should "have the correct info for a method body" in {
    val commit = add(FILE_NAME, "public class A{\npublic void m(){\nint x=3;}}}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 3)
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",2,method(2:3),Block,A,m,")
  }

  it should "have the correct info for a static block" in {
    val commit = add(FILE_NAME, "public class A{\n{int x=3;\n}}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 2)
    new StatementInfo(FILE_NAME, node).printInfo should equal (FILE_NAME + ",2,static(2:3),Block,A,,")
  }

  it should "have the correct info for a method" in {
    val commit = add(FILE_NAME, "public class A{\npublic void m(){\nint x=3;\n}}")
    val node = MethodFinder.findNode(Git.open(repo), commit.getName, FILE_NAME, 3)
    new StatementInfo(FILE_NAME, node, 3).printInfo should equal (FILE_NAME + ",3,m,MethodDeclaration,A,m,")
  }
}
