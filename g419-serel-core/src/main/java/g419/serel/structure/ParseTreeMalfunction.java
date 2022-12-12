package g419.serel.structure;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParseTreeMalfunction {

  public enum MalfunctionCode { AHPI, MEPO };



  MalfunctionCode malfunctionCode;


  String documentPath;

  String annotationId;
  int sourceIndex;
  int targetIndex;
  int annStartRange;
  int annEndRange;

}
