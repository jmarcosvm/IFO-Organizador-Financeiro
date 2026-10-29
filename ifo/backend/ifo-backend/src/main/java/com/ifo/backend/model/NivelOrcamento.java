package com.ifo.backend.model;

// OK < 80%, ATENCAO de 80% a 100%, ESTOURO > 100% (RN07); SEM_LIMITE quando a renda é 0 (RN06)
public enum NivelOrcamento {
    OK,
    ATENCAO,
    ESTOURO,
    SEM_LIMITE
}
