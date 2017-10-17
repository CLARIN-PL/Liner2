package g419.liner2.core.tools;

public class QuotationSize {

	/* Długość sekwencji rozpoczynającej cytot w tokenach */
	private int openingQuotationLength = 0;
	
	/* Długość cytatu w tokenach */
	private int textLength = 0;
	
	/* Długosć sekwencji kończącej cytat w tokenach */
	private int closingQuotationLength = 0;
	
	public QuotationSize(int openingLength, int textLength, int closingLength){
		this.openingQuotationLength = openingLength;
		this.textLength = textLength;
		this.closingQuotationLength = closingLength;
	}
	
	public int getOpeningLength(){
		return this.openingQuotationLength;
	}
	
	public int getTextLength(){
		return this.textLength;
	}
	
	public int getClosingLength(){
		return this.closingQuotationLength;
	}
	
	public int getTotalLength(){
		return this.openingQuotationLength + this.textLength + this.closingQuotationLength;
	}
}
