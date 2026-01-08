package ch.hearc.ig.guideresto.business;

/**
 * Contrat minimal des objets métier persistés.
 * Fournit l'identifiant utilisé par l'Identity Map des mappers.
 */
public interface IBusinessObject {
    /**
     * @return identifiant technique (null si non persisté)
     */
    Integer getId();
}
