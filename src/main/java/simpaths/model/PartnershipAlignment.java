package simpaths.model;

import microsim.engine.SimulationEngine;
import simpaths.data.IEvaluation;
import simpaths.data.Parameters;
import simpaths.model.enums.Dcpst;
import simpaths.model.enums.TargetShares;

import java.util.Set;

/**
 * PartnershipAlignment adjusts the probability of individuals forming a union to values observed in the data.
 * It modifies the intercept of the "considerCohabitation" probit model.
 *
 * To find the value by which the intercept should be adjusted, it uses a search routine.
 * The routine adjusts probabilities, and performs union matching separate from the real matching in the model. If the
 * results differ from the targets based on the data by more than a specified threshold, the adjustment is repeated.
 *
 * Importantly, the adjustment needs to be only found once. Modified intercepts can then be used in subsequent simulations.
 */

public class PartnershipAlignment implements IEvaluation {

    private double targetAggregateShareOfPartneredPersons;
    private double partnershipAdjustment;
    boolean partnershipAdjustmentChanged;
    private Set<Person> persons;
    private SimPathsModel model;

    public PartnershipAlignment(Set<Person> persons, double partnershipAdjustment) {
        this.model = (SimPathsModel) SimulationEngine.getInstance().getManager(SimPathsModel.class.getCanonicalName());
        this.persons = persons;
        this.partnershipAdjustment = partnershipAdjustment;
        targetAggregateShareOfPartneredPersons = Parameters.getTargetShare(model.getYear(), TargetShares.Partnership);
    }

    /**
     * Evaluates the discrepancy between the simulated and target aggregate share of partnered persons and adjusts partnerships if necessary.
     *
     * This method compares the adjustment parameter 'args[0]' with the current 'partnershipAdjustment'.
     * If the absolute difference exceeds a small threshold (1.0E-5), it triggers the adjustment of partnerships.
     *
     * The error is then calculated as the difference between the target and the actual aggregate share of partnered persons.
     * This error value is returned and serves as the stopping condition in root search routines.
     *
     * @param args An array of parameters, where args[0] represents the adjustment parameter.
     * @return The error in the target aggregate share of partnered persons after potential adjustments.
     */
    @Override
    public double evaluate(double[] args) {

        model.clearPersonsToMatch();
        persons.stream()
                .filter(person -> person.getDag() >= Parameters.MIN_AGE_COHABITATION)
                .forEach(person -> person.evaluatePartnershipDissolution());

        adjustPartnerships(args[0]);

        double error = targetAggregateShareOfPartneredPersons - evalAggregateShareOfPartneredPersons();
        return error;
    }

    /**
     * Evaluates the aggregate share of persons with partners assigned in a test run of union matching among those eligible for partnership.
     *
     * This method uses Java streams to count the number of persons who meet the age criteria for cohabitation
     * and the number of persons who currently have a test partner. The aggregate share is calculated as the
     * ratio of successfully partnered persons to those eligible for partnership, with consideration for potential division by zero.
     *
     * @return The aggregate share of partnered persons among those eligible, or 0.0 if no eligible persons are found.
     */
    private double evalAggregateShareOfPartneredPersons() {
        long numPersonsWhoCanHavePartner = persons.stream()
                .filter(person -> person.getDag() >= Parameters.MIN_AGE_COHABITATION)
                .count();

        long numPersonsPartnered = persons.stream()
                .filter(person -> (person.hasTestPartner() || (person.getDcpst().equals(Dcpst.Partnered)) && !person.hasLeftPartnerTest()))
                .count();

        return numPersonsWhoCanHavePartner > 0
                ? (double) numPersonsPartnered / numPersonsWhoCanHavePartner
                : 0.0;
    }

    /**
     * Adjusts the probit regression used for partnership evaluation and re-evaluates the score for all eligible persons.
     * Then, creates "test" unions between individuals.
     *
     * This method performs the following steps:
     * 1. Runs the cohabitation probit model.
     * 2. Matches individuals within this method.
     * TODO: the loops over persons calculating cohabitation probability should be paralellised
     * @param newPartnershipAdjustment The new adjustment value for the partnership probit regression.
     */
    private void adjustPartnerships(double newPartnershipAdjustment) {
        persons.parallelStream()
                .filter(person -> person.getDag() >= Parameters.MIN_AGE_COHABITATION)
                .forEach(person -> person.evaluatePartnershipFormation(newPartnershipAdjustment));

        // "Fake" union matching (not modifying household structure) here
        model.unionMatching(true);
        model.unionMatchingNoRegion(true);

        partnershipAdjustment = newPartnershipAdjustment;
        partnershipAdjustmentChanged = true;
    }

}
