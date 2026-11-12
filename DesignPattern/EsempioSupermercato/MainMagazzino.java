public class MainMagazzino {
    public static void main(String[] args) {
        Magazzino.getInstance().aggiungi(new Prodotto("Mouse-01", "Mouse", 25));
        Magazzino.getInstance().aggiungi(new Prodotto("MON-27", "Monitor", 4));

        Magazzino magazzino = Magazzino.getInstance();
        System.out.println(magazzino.elencoCopia().size());
        System.out.println(magazzino.elencoCopia());

        Magazzieniere mag1 = new Magazzieniere();
        mag1.aggiugniProdottoAlMagazzino();

        System.out.println(magazzino.elencoCopia());

    }
}
