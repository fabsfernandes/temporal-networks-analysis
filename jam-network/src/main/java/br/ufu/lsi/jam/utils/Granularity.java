package br.ufu.lsi.jam.utils;

public enum Granularity {
    
    DAY {
        @Override
        public String toString() {
          return "DAY";
        }
    },
    MONTH {
        @Override
        public String toString() {
          return "MONTH";
        }
    },
    QUARTER_YEAR {
        @Override
        public String toString() {
          return "TRIMESTER";
        }
    },
    SEMESTER{
        @Override
        public String toString() {
          return "SEMESTER";
        }
    },
    YEAR {
        @Override
        public String toString() {
          return "YEAR";
        }
    };
    
    

}
