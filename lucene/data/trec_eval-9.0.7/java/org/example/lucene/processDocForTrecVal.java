import java.io.*;

public class processDocForTrecVal
{
    public static void main(String[] args) throws IOException
    {

        //BufferedReader是可以按行读取文件
        FileInputStream inputStream = new FileInputStream("data/cranqrel");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        Writer writer = new FileWriter(new File("data/test/qrel.test"));

        String str = null;
        String qryNum = null;
        while((str = bufferedReader.readLine()) != null) {
            String arr[] = str.split("\s+");
            writer.write(arr[0] + "\s" + "0" + "\s" + arr[1] + "\s" + arr[2] + "\n");
            System.out.println(arr[0] + "\s" + "0" + "\s" + arr[1] + "\s" + arr[2] + "\n");

            String s = "data/question/"+arr[0] + ".txt";
            System.out.println(s);

            File file = new File(s);

            File fileParent = file.getParentFile();
            if(!fileParent.exists()){
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(s,true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(arr[1]);
            bufferWritter.write('\n');
            bufferWritter.close();
           }
        //close
        writer.close();
        inputStream.close();
        bufferedReader.close();
    }
}