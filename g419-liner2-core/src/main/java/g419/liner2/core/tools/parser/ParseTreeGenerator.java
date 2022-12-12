package g419.liner2.core.tools.parser;

import g419.corpus.structure.Sentence;

public interface ParseTreeGenerator {

  ParseTree generate(Sentence sentence) throws Exception;

}
