package id.ac.tazkia.dosen.controller;

import id.ac.tazkia.dosen.dao.KegiatanPendidikanDao;
import id.ac.tazkia.dosen.dao.MataKuliahDao;
import id.ac.tazkia.dosen.entity.BuktiKinerja;
import id.ac.tazkia.dosen.entity.BuktiPenugasan;
import id.ac.tazkia.dosen.entity.KinerjaPendidikan;
import id.ac.tazkia.dosen.service.ImageService;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/kinerja/pendidikan")
public class KinerjaBidangPendidikanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KinerjaBidangPendidikanController.class);

    @Autowired
    private KegiatanPendidikanDao kegiatanPendidikanDao;

    @Autowired
    private MataKuliahDao mataKuliahDao;

    @Autowired
    private ImageService imageService;

    private final List<String> FILE_EXTENSION = Arrays.asList("png", "jpg", "jpeg");

    @GetMapping("/list")
    public String kegiatanPendidikanList(@PageableDefault(size = 10) Pageable pageable, ModelMap mm) {
        PageRequest page = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "periode");
        mm.addAttribute("data", kegiatanPendidikanDao.findAll(page));
        return "kinerja/pendidikan/list";
    }

    @GetMapping("/form")
    public String tampilkanForm(@RequestParam(required = false) String id, ModelMap mm) {
        mm.addAttribute("listMatkul", mataKuliahDao.findAll());
        KinerjaPendidikan kegiatan = new KinerjaPendidikan();
        if (id != null && !id.isEmpty()) {
            kegiatan = kegiatanPendidikanDao.findOne(id);
        } else {
            BuktiKinerja buktiKinerja = new BuktiKinerja();
            BuktiPenugasan buktiPenugasan = new BuktiPenugasan();
            kegiatan.setBuktiKinerja(buktiKinerja);
            kegiatan.setBuktiPenugasan(buktiPenugasan);
        }

        mm.addAttribute("kinerja", kegiatan);
        return "kinerja/pendidikan/form";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id, ModelMap mm) {
        KinerjaPendidikan kegiatan = new KinerjaPendidikan();
        if (id != null && !id.isEmpty()) {
            kegiatan = kegiatanPendidikanDao.findOne(id);
            kegiatanPendidikanDao.delete(kegiatan);
        }

        return "redirect:/kinerja/pendidikan/list";
    }

    @PostMapping("/form")
    public String prosesForm(@Valid KinerjaPendidikan kinerja, ModelMap mm, BindingResult errors,
            MultipartFile filePenugasan, MultipartFile fileKinerja, HttpServletRequest request) {

        if (filePenugasan != null && !filePenugasan.isEmpty()) {
            if (filePenugasan.getSize() > 2097152) {
                LOGGER.info("UPLOAD GAGAL");
                LOGGER.info("BESAR FILE YANG DI UPLOAD === [{}]", filePenugasan.getSize());
                LOGGER.info("MAXIMUM BESAR FILE === [{}]", 2097152);

                errors.addError(new FieldError("buktiPenugasan.nama", "buktiPenugasan.nama", "File terlalu besar, max 2mb"));
            } else {
                String extention = tokenizer(filePenugasan.getOriginalFilename(), ".");
                if (FILE_EXTENSION.contains(extention.toLowerCase())) {
                    File file = imageService.moveFile(filePenugasan, "bukti-penugasan", extention);
                    kinerja.getBuktiPenugasan().setUrl(file.getName());
                } else {
                    errors.addError(new FieldError("buktiPenugasan.nama", "buktiPenugasan.nama", "File yang diperbolehkan png, jpg, jpeg"));
                }
            }
        }

        if (fileKinerja != null && !fileKinerja.isEmpty()) {
            if (fileKinerja.getSize() > 2097152) {
                LOGGER.info("UPLOAD GAGAL");
                LOGGER.info("BESAR FILE YANG DI UPLOAD === [{}]", fileKinerja.getSize());
                LOGGER.info("MAXIMUM BESAR FILE === [{}]", 2097152);

                errors.addError(new FieldError("buktiKinerja.nama", "buktiKinerja.nama", "File terlalu besar, max 2mb"));
            } else {
                String extention = tokenizer(fileKinerja.getOriginalFilename(), ".");
                if (FILE_EXTENSION.contains(extention.toLowerCase())) {
                    File file = imageService.moveFile(fileKinerja, "bukti-kinerja", extention);
                    kinerja.getBuktiKinerja().setUrl(file.getName());
                } else {
                    errors.addError(new FieldError("buktiKinerja.nama", "buktiKinerja.nama", "File yang diperbolehkan png, jpg, jpeg"));
                }
            }
        }

        if (errors.getErrorCount() > 0) {
            mm.addAttribute("kinerja", kinerja);
            mm.addAttribute("listMatkul", mataKuliahDao.findAll());
            return "kinerja/pendidikan/form";
        }
        
        if(kinerja.getId() != null){
            LOGGER.info("ID [{}]", kinerja.getId());
        }
        kegiatanPendidikanDao.save(kinerja);
        return "redirect:/kinerja/pendidikan/list";
    }

    private String tokenizer(String originalFilename, String token) {
        StringTokenizer tokenizer = new StringTokenizer(originalFilename, token);
        String result = "";
        while (tokenizer.hasMoreTokens()) {
            result = tokenizer.nextToken();
        }
        return result;
    }
}
