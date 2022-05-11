package me.Jedi.scoring;

import me.Jedi.util.Letters;
import me.Jedi.util.WordData;

import java.util.*;


public class WordProperties {

    public static Map<me.Jedi.scoring.WordProperties.LetterProperty, List<WordData>> getWordProperties(List<WordData> words) {
        Character firstkey = 'I';
        Map<me.Jedi.scoring.WordProperties.LetterProperty, List<WordData>> propertites = new HashMap<>();
        for(int i=0; i<2; i++) {
            for(Character c :Letters.letters.keySet()) {
                List<WordData> meetsConditions = new ArrayList<>();

                for(WordData d : words) {
                    if(firstkey.equals('I') && d.getLetters().contains(c)) meetsConditions.add(d);
                    else if(firstkey.equals('N') && !d.getLetters().contains(c)) meetsConditions.add(d);
                }

                propertites.put(LetterProperty.valueOf(firstkey + c.toString()), meetsConditions);
            }
            firstkey = 'N';
        }

        return propertites;
    }

    public enum LetterProperty {
        Ia, Ib, Ic, Id, Ie, If, Ig, Ih, Ii, Ij, Ik, Il, Im, In, Io, Ip, Iq, Ir, Is, It, Iu, Iv, Iw, Ix, Iy, Iz,
        Na, Nb, Nc, Nd, Ne, Nf, Ng, Nh, Ni, Nj, Nk, Nl, Nm, Nn, No, Np, Nq, Nr, Ns, Nt, Nu, Nv, Nw, Nx, Ny, Nz,
    }
}
