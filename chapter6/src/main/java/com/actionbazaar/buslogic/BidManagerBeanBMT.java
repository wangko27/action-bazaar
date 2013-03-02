/**
 * BidManagerBeanBMT.java EJB 3 in Action Book: http://manning.com/panda2/ Code:
 * http://code.google.com/p/action-bazaar/ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.actionbazaar.buslogic;

import com.actionbazaar.account.Bidder;
import com.actionbazaar.buslogic.exceptions.CreditCardSystemException;
import com.actionbazaar.buslogic.exceptions.CreditProcessingException;
import com.actionbazaar.model.Bid;
import com.actionbazaar.model.CreditCard;
import com.actionbazaar.model.Item;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

/**
 * Demonstrates the BidManagerBean using Bean Managed Transactions (BMT).
 */
@RunAs("ADMIN")
@Stateless(name = "BidManagerBMT")
@TransactionManagement(TransactionManagementType.BEAN)
public class BidManagerBeanBMT implements BidManager {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger("BidManagerBean");
    
    /**
     * Credit Card Manager
     */
    @EJB
    private CreditCardManager creditCardManager;
    
    /**
     * User Transaction
     */
    @Resource
    private UserTransaction userTransaction;
    
    /**
     * EJB Session context
     */
    @Resource
    private SessionContext context;

    /**
     * Places a SnagItOrder
     * @param item - item being ordered
     * @param bidder - bidder
     * @param card - card
     */
    public void placeSnagItOrder(Item item, Bidder bidder, CreditCard card) {
        try {
            userTransaction.begin();
            if (!hasBids(item)) {
                creditCardManager.validateCard(card);
                creditCardManager.chargeCreditCard(card, item.getInitialPrice());
                closeBid(item, bidder, item.getInitialPrice());
            }
            userTransaction.commit();
        } catch (CreditProcessingException ce) {
            logger.log(Level.SEVERE, "An error ocurred processing the order.", ce);
            context.setRollbackOnly();
        } catch (CreditCardSystemException ccse) {
            logger.log(Level.SEVERE, "Unable to validate credit card.", ccse);
            context.setRollbackOnly();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error ocurred processing the order.", e);
        }
    }
    
    /**
     * Closes a bid
     * @param item - item being purchased
     * @param bidder - bidder
     * @param initialPrice - initial price
     */
    public void closeBid(Item item, Bidder bidder, BigDecimal initialPrice ) throws CreditCardSystemException {
        
    }
    
    /**
     * Checks to see if bids exist
     * @param item - item
     * @return true if the item has bids
     */
    protected boolean hasBids(Item item) {
        return false;
    }
    
    /**
     * Cancels a bid
     * @param bid - bid to be canceled
     */
    @RolesAllowed({"CSR", "ADMIN"})
    public void cancelBid(Bid bid) {
    }

    /**
     * Returns the bids
     * @param item - item
     * @return bids
     */
    @PermitAll
    public List<Bid> getBids(Item item) {
        return item.getBids();
    }

}
