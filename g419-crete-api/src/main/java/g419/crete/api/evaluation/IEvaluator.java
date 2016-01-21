package g419.crete.api.evaluation;

import g419.corpus.structure.Document;

public interface IEvaluator {
    void evaluate(Document systemDocument, Document referenceDocument);
    void printTotal();
}
