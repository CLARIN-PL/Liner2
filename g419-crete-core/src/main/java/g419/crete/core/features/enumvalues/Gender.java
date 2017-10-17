package g419.crete.core.features.enumvalues;

public enum Gender {
		UNDEFINED,
		NEUTER,
		FEMINUM,
		MASCULINUM,
		MASCULINUM_1,
		MASCULINUM_2,
		MASCULINUM_3;
		
		public static Gender fromValue(String value){
			if("n".equals(value)) return NEUTER;
			if("m".equals(value)) return MASCULINUM;
			if("m1".equals(value)) return MASCULINUM_1;
			if("m2".equals(value)) return MASCULINUM_2;
			if("m3".equals(value)) return MASCULINUM_3;
			if("f".equals(value)) return FEMINUM;
			return UNDEFINED;
		}
		
		public static Gender fromValueBroad(String value){
			if("n".equals(value)) return NEUTER;
			if("m1".equals(value) || "m2".equals(value) || "m3".equals(value) || "m".equals(value)) return MASCULINUM_1;
			if("f".equals(value)) return FEMINUM;
			return UNDEFINED;
		}
		
		public boolean equalsSoftMasculinum(Gender other){
			if(FEMINUM.equals(other) || NEUTER.equals(other) || UNDEFINED.equals(other)) return equals(other);
			if(FEMINUM.equals(this) || NEUTER.equals(this) || UNDEFINED.equals(this)) return equals(other);
			return true;
		}
}
