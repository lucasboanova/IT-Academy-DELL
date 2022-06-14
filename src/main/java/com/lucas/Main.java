package com.lucas;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    //Comando usado para printar no console utilizando acentuação.
    static PrintStream printStream = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    //Iniciando scanner e utilizando ".useDelimiter" para mudar o delimitador padrão para tecla enter.
    static Scanner input = new Scanner(System.in).useDelimiter("\n");
    static List<String[]> medicines;

    public static void main(String[] args) throws IOException, CsvException {
        Path myPath = Paths.get("src/main/resources/TA_PRECO_MEDICAMENTO.csv");
        //Alterando o Charset correto para utilização de caracteres especiais.
        BufferedReader bufferedReader = Files.newBufferedReader(myPath, StandardCharsets.ISO_8859_1);
        //Declarando ";" como separador.
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader csvReader = new CSVReaderBuilder(bufferedReader).withCSVParser(parser).build();
        //Pulando a 1° linha do arquivo (Header).
        csvReader.skip(1);

        String userChoice = "";
        //Leitura do arquivo CSV.
        medicines = csvReader.readAll();
        //Utilizando While para utilização do menu principal da aplicação.
        while (!userChoice.equals("4")) {

            printStream.println("-------------------------------MENU------------------------------");
            printStream.println("1 - Consultar item pelo nome.");
            printStream.println("2 - Consultar item pelo codigo de barras.");
            printStream.println("3 - Exibir tabela com impostos PIS/COFINS.");
            printStream.println("4 - Encerrar programa.");
            printStream.println("------------------------------------------------------------------");
            //Utilizando Switch case para direcionar a função escolhida pelo usuário.
            userChoice = input.next();
            switch (userChoice) {
                case "1" -> consultMedicine();
                case "2" -> pmcDifference();
                case "3" -> taxList();
                case "4" -> printStream.println("Programa encerrado");
                default -> printStream.println("Digite novamente um número entre 1 e 4");
            }
        }
    }

    //Metodo em que o usuário pode pesquisar usando parte do nome ou ele inteiro, podendo pesquisar com letra maiúscula ou minúscula.
    private static void consultMedicine() {
        String name;
        int register = 0;

        printStream.println("Digite o nome do medicamento por parte ou inteiro: ");
        name = input.next();

        for (int i = 0; i < medicines.size(); i++) {
            //Verificando se os nome pertence a um medicamento e validando se ele foi comercializado em 2020.
            if (medicines.get(i)[0].toLowerCase().contains(name.toLowerCase()) && medicines.get(i)[38].equalsIgnoreCase("Sim")) {
                register++;
                printStream.println("Nome: " + medicines.get(i)[0]);
                printStream.println("Produto: " + medicines.get(i)[8]);
                printStream.println("Apresentação: " + medicines.get(i)[9]);
                printStream.println("PF sem imposto: " + medicines.get(i)[13]);
                printStream.println("");
            }
        }
        //Printando caso não haja registro encontrado.
        if (register == 0) {
            printStream.println("Nenhum registro encontrado contendo: " + name);
        }
    }

    //Metodo em que o usuário pesquisa pelo código de barras do produto, mostrando o PMC menor, maior e sua diferença.
    private static void pmcDifference() {
        float difference;
        String barCode;
        int register = 0;
        printStream.println("Informe o código de barras do medicamento:");
        barCode = input.next();

        for (int i = 0; i < medicines.size(); i++) {
            if (medicines.get(i)[5].equalsIgnoreCase(barCode)) {
                //Utilização do replace para trocar as virgulas dos números por pontos para poder calcular sua diferença.
                difference = Float.parseFloat(medicines.get(i)[31].replace(",", ".")) - Float.parseFloat(medicines.get(i)[23].replace(",", "."));
                printStream.println("Código de barras: " + barCode);
                printStream.println("PMC menor: " + medicines.get(i)[23].replace(",", "."));
                printStream.println("PMC maior: " + medicines.get(i)[31].replace(",", "."));
                printStream.printf("Diferença entre PMCs: %.2f \n", difference);
                printStream.println("");
            }
        }
        //Printando caso não haja registro encontrado.
        if (register == 0) {
            printStream.println("Nenhum registro encontrado com o código de barras: " + barCode);
        }
    }

    //Metodo que exibe lista de medicamentos comercializados em 2020, e com PIS/COFINS Negativo, Neutro e Positivo.
    private static void taxList() {
        int neutralCount = 0;
        int positiveCount = 0;
        int negativeCount = 0;
        int acceptCount = 0;
        float negativePercentage;
        float neutralPercentage;
        float positivePercentage;
        //Percorrendo o CSV, e ja gravando na memória o resultado total.
        for (int i = 0; i < medicines.size(); i++) {
            if (medicines.get(i)[38].equalsIgnoreCase("Sim")) {
                acceptCount++;
                switch (medicines.get(i)[37]) {
                    case "Negativa" -> negativeCount++;
                    case "Neutra" -> neutralCount++;
                    case "Positiva" -> positiveCount++;
                }
            }
        }
        //Dividindo o resultado total de cada categoria pelo total encontrado, e multiplicando por 100 para aplicar na porcentagem do gráfico.
        negativePercentage = (float) negativeCount / acceptCount * 100;
        neutralPercentage = (float) neutralCount / acceptCount * 100;
        positivePercentage = (float) positiveCount / acceptCount * 100;
        //Printando no console o gráfico.
        printStream.println("Classificação\t\tPercentual\t\tGráfico");
        printStream.printf("Negativa\t\t\t" + String.format("%.2f", negativePercentage) + "%%\t\t\t" + createGraphic(negativePercentage) + "\n");
        printStream.printf("Neutra\t\t\t\t" + String.format("%.2f", neutralPercentage) + "%%\t\t\t" + createGraphic(neutralPercentage) + "\n");
        printStream.printf("Positiva\t\t\t" + String.format("%.2f", positivePercentage) + "%%\t\t\t" + createGraphic(positivePercentage) + "\n");
        printStream.println("Total\t\t\t\t100%\n");
    }

    //Metodo que obtém o gráfico com asteriscos de acordo com porcentagem.
    private static String createGraphic(Float percentage) {
        String text = "";
        for (int i = 0; i < percentage; i++) {
            text = text + "*";
        }
        return text;
    }
}