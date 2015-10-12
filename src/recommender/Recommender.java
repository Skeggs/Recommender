/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

import org.slf4j.LoggerFactory;
import org.apache.mahout.cf.taste.impl.model.AbstractIDMigrator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;

/**
 *
 * @author rskeggs
 */
public class Recommender {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            // TODO code application logic here
            //DataModel model = new FileDataModel(new File("c:/users/rskeggs/Desktop/lotto-draw-history.csv"));
            
            DataModel model = new AlphaItemFileDataModel(new File("c:/users/rskeggs/Desktop/lotto-draw-history.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List recommendations = recommender.recommend(2, 3);
            for (Object recommendation : recommendations)
            {
                System.out.println(recommendation);
            }
        }
        catch(TasteException te){ te.printStackTrace(); }
        catch(IOException io){ io.printStackTrace(); }
    }
    
}


//import java.io.File;
//import java.io.IOException;

//import org.apache.mahout.cf.taste.common.TasteException;
//import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

class AlphaItemFileDataModel extends FileDataModel 
{
      private final ItemMemIDMigrator  memIdMigtr = new ItemMemIDMigrator();
     
      public AlphaItemFileDataModel(File dataFile) throws IOException 
      {
            super(dataFile);       
      }

      public AlphaItemFileDataModel(File dataFile, String transpose) throws IOException 
      {
            super(dataFile, transpose);
      }

      @Override
      protected long readItemIDFromString(String value) 
      {
            long retValue =  memIdMigtr.toLongID(value);
            if(null == memIdMigtr.toStringID(retValue))
            {
                  try 
                  {
                        memIdMigtr.singleInit(value);
                  } 
                  catch (TasteException e) { e.printStackTrace(); }
            }
            return retValue;
      }
   
      String getItemIDAsString(long itemId)
      {
            return memIdMigtr.toStringID(itemId);
      }
}

//import org.apache.mahout.cf.taste.common.TasteException;



      class ItemMemIDMigrator extends AbstractIDMigrator 
      {
       
        private final FastByIDMap<String> longToString;
       
        public ItemMemIDMigrator() 
        {
          this.longToString = new FastByIDMap<String>(100);
        }
       
        //@Override
        public void storeMapping(long longID, String stringID)
        {
          synchronized (longToString) {
            longToString.put(longID, stringID);
          }
        }
       
        @Override
        public String toStringID(long longID)
        {
          synchronized (longToString) 
          {
            return longToString.get(longID);
          }
        }
        
        public void singleInit(String stringID) throws TasteException 
        {
            storeMapping(toLongID(stringID), stringID);
        }
       
      }
