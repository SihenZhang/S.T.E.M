package brightspark.stem.recipe;

import brightspark.stem.STEM;
import brightspark.stem.util.LogHelper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerRecipeManager
{
    public static final char csvSeparator = ',';
    private static File stemRecipeFile;
    private static List<StemRecipe> recipes;

    public static boolean addRecipe(StemRecipe recipe)
    {
        StemRecipe existingRecipe = getRecipeForStack(recipe.getOutput());
        if(existingRecipe == null)
            recipes.add(recipe);
        else
        {
            if(existingRecipe.getFluidInput() == recipe.getFluidInput())
                return false;
            else
                existingRecipe.setFluidInput(recipe.getFluidInput());
        }
        return true;
    }

    public static boolean removeRecipe(ItemStack stack)
    {
        for(int i = 0; i < recipes.size(); i++)
            if(recipes.get(i).isStackEqual(stack))
            {
                recipes.remove(i);
                return true;
            }
        return false;
    }

    public static List<StemRecipe> getRecipes()
    {
        return recipes;
    }

    /**
     * Checks if a recipe exists for the given item.
     */
    public static boolean hasRecipeForStack(ItemStack stack)
    {
        return getRecipeForStack(stack) != null;
    }

    /**
     * Gets the recipe for the given item stack.
     * Returns null if no recipe is found.
     */
    public static StemRecipe getRecipeForStack(ItemStack stack)
    {
        if(stack == null)
            return null;
        if(recipes == null)
            return new StemRecipe(stack, 0);
        for(StemRecipe recipe : recipes)
            if(recipe.isStackEqual(stack))
                return recipe;
        return new StemRecipe(stack, 0);
    }

    /**
     * Get the amount of S.T.E.M needed to make the item.
     * Returns 0 if there's no recipe for the item.
     */
    public static int getStemNeeded(ItemStack stack)
    {
        for(StemRecipe recipe : recipes)
            if(recipe.isStackEqual(stack))
                return recipe.getFluidInput();
        return 0;
    }

    /**
     * Saves the recipes currently in the array to the recipe file.
     */
    public static void saveRecipes()
    {
        CSVWriter writer;
        try
        {
            writer = new CSVWriter(new FileWriter(stemRecipeFile), csvSeparator);
        }
        catch(IOException e)
        {
            LogHelper.error("Couldn't create recipe file!");
            return;
        }

        //Write 'header'
        writer.writeNext(new String[] {"Put in here the recipes for converting S.T.E.M fluid into items"});
        writer.writeNext(new String[] {"Output Item ID", "Output Item Metadata", "Input fluid amount"});
        writer.writeNext(new String[] {""});

        //Write recipes
        for(StemRecipe r : recipes)
            writer.writeNext(r.toCsvStringArray());

        try
        {
            writer.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Couldn't close CSV Writer");
        }

        LogHelper.info("Saved " + recipes.size() + " recipes successfully.");
    }

    public static void init()
    {
        recipes = new ArrayList<>();
        stemRecipeFile = new File(STEM.CONFIG_DIR, "recipes.csv");
    }

    private static void closeReader(CSVReader reader)
    {
        try
        {
            reader.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Couldn't close CSV Reader");
        }
    }

    public static void readRecipeFile()
    {
        if(!stemRecipeFile.exists())
        {
            //If recipe file doesn't exist, then create the default file.
            createDefaultRecipeFile();
        }
        else
        {
            //If recipe file does exist, then read it and use it for the recipes.
            LogHelper.info("Reading existing recipe file...");

            CSVReader reader;
            try
            {
                reader = new CSVReader(new FileReader(stemRecipeFile), csvSeparator, '\"', 3);
            }
            catch(IOException e)
            {
                LogHelper.error("Couldn't get recipe file! It may not exist or be inaccessible. Creating default recipe file.");
                createDefaultRecipeFile();
                return;
            }

            //Try read the file
            List<String[]> fromFile;
            try
            {
                fromFile = reader.readAll();
            }
            catch(IOException e)
            {
                LogHelper.error("Couldn't read recipe file! Creating default recipe file.");
                closeReader(reader);
                createDefaultRecipeFile();
                return;
            }
            closeReader(reader);

            //Save recipes from file as recipe objects in the recipe array
            recipes.clear();
            for(String[] r : fromFile)
                recipes.add(StemRecipe.fromCsvStringArray(r));

            LogHelper.info("Recipe file read successfully.");

            for(StemRecipe r : recipes)
                LogHelper.info(r.toString());
        }
    }

    private static void createDefaultRecipeFile()
    {
        LogHelper.info("Creating default recipe file...");

        CSVWriter writer;
        try
        {
            writer = new CSVWriter(new FileWriter(stemRecipeFile), csvSeparator);
        }
        catch(IOException e)
        {
            LogHelper.error("Couldn't create default recipe file!");
            return;
        }

        //Write 'header'
        writer.writeNext(new String[] {"Put in here the recipes for converting S.T.E.M fluid into items"});
        writer.writeNext(new String[] {"Output Item ID", "Output Item Metadata", "Input fluid amount"});
        writer.writeNext(new String[] {""});

        //Write recipes
        for(StemRecipe r : recipes)
            writer.writeNext(r.toCsvStringArray());

        try
        {
            writer.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Couldn't close CSV Writer");
        }

        LogHelper.info("Recipe file created successfully.");
    }

    /**
     * Creates the default recipe file and reads it back.
     */
    public static void resetRecipeFile()
    {
        createDefaultRecipeFile();
        readRecipeFile();
    }
}
