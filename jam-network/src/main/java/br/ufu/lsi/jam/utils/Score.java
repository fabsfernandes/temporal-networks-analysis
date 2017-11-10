package br.ufu.lsi.jam.utils;

public enum Score {
    
    AVERAGE {
        @Override
        public String toString() {
          return "AVERAGE";
        }
    },
    RANKING {
        @Override
        public String toString() {
          return "RANKING";
        }
    },
    Z {
        @Override
        public String toString() {
          return "Z";
        }
    };
    
    

}
