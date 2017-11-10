package br.ufu.lsi.jam.utils;

public enum Centrality {
    
    CLOSENESS {
        @Override
        public String toString() {
          return "CLOSENESS";
        }
    },
    BETWEENNESS {
        @Override
        public String toString() {
          return "BETWEENNESS";
        }
    };
}
