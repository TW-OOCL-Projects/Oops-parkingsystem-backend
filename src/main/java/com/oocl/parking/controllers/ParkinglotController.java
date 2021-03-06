package com.oocl.parking.controllers;


import com.oocl.parking.dto.ParkinglotDto;
import com.oocl.parking.entities.Orders;
import com.oocl.parking.entities.Parkinglot;
import com.oocl.parking.exceptions.BadRequestException;
import com.oocl.parking.services.ParkinglotService;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/parkinglots")
public class ParkinglotController {

    private ParkinglotService parkinglotService;

    @Autowired
    public ParkinglotController(ParkinglotService parkinglotService){
        this.parkinglotService = parkinglotService;
    }

    @GetMapping(path = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParkinglotDto> getDashboard(
            @PageableDefault(value = 100, sort = {"id"}, direction = Sort.Direction.ASC) Pageable page){
        List<ParkinglotDto> parkinglotDtos = parkinglotService.getDashboard(page, "open");
        if(parkinglotDtos.size() == 0){
            throw new BadRequestException("no parking lots available");
        }
        return parkinglotDtos;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParkinglotDto> getAllParkinglots(
                @PageableDefault(value = 100, sort = {"id"}, direction = Sort.Direction.ASC)Pageable page,
            @RequestParam(required = false, value = "status") Optional<String> status){
        String state = status.orElse(null);
        List<ParkinglotDto> parkinglotDtos = parkinglotService.getAllParkinglots(page, state);
        if(parkinglotDtos.size() == 0){
            throw new BadRequestException("no parking lots available");
        }
        return parkinglotDtos;
    }

    @GetMapping(path = "/noUser", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParkinglotDto> getNoUserParkinglots(
            @PageableDefault(value = 100, sort = {"id"}, direction = Sort.Direction.ASC)Pageable page,
            @RequestParam String status
            ){
        return parkinglotService.getNoUserParkinglots(page, status);
    }

    @GetMapping(path = "/combineSearch", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParkinglotDto> getParkinglots(
            @PageableDefault(value = 100, sort = {"id"}, direction = Sort.Direction.ASC)Pageable page,
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<String> tel,
            @RequestParam(required = false) Optional<Integer> sizeBt,
            @RequestParam(required = false) Optional<Integer> sizeSt
            ){
        String _name = name.orElse(null);
        String _tel = tel.orElse(null);
        int bt = sizeBt.orElse(0);
        int st = sizeSt.orElse(Integer.MAX_VALUE);

        return parkinglotService.getPakinglotsCombineSearch(page, _name, _tel, bt, st);

    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkinglotDto createParkinglot(@RequestBody Parkinglot parkinglot){
        ParkinglotDto parkinglotDto = parkinglotService.save(parkinglot);
        if(parkinglotDto != null){
            return parkinglotDto;
        }
        throw new BadRequestException("停车场创建失败");
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkinglotDto getById(@PathVariable Long id){
        ParkinglotDto parkinglotDto = parkinglotService.getById(id);
        if(parkinglotDto == null){
            throw new BadRequestException();
        }
        return parkinglotDto;
    }

    @PatchMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity changeStatusById(@PathVariable Long id){
        if(parkinglotService.changeStatusById(id)){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        throw new BadRequestException();
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkinglotDto modifyById(@PathVariable Long id, @RequestBody Parkinglot parkinglot){
        ParkinglotDto parkinglotDto =
            parkinglotService.changeNameById(id, parkinglot.getName(), parkinglot.getSize());
        if(parkinglotDto != null) {
            return parkinglotDto;
        }
        throw new BadRequestException("modify failed");
    }

    @PutMapping(path = "/{id}/park", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParkinglotDto park(@PathVariable(value = "id") Long id){
        ParkinglotDto parkinglotDto = parkinglotService.park(id);
        if( parkinglotDto != null){
            return parkinglotDto;
        }
        throw new BadRequestException("parked failed");
    }

    @DeleteMapping(path = "/{id}/park/{parkingLotId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unpark(@PathVariable(value = "id") Long id ,@PathVariable Long parkingLotId){
        if(parkinglotService.unpark(id,parkingLotId)){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        throw new BadRequestException("unpark failed");
    }
//    @GetMapping(path = "",produces = MediaType.APPLICATION_JSON_VALUE)
//    public  List<ParkinglotDto> findParkingLotsByParkingBoy(){
//
//    }

    @GetMapping(path = "/{id}/orders")
    public List<Orders> getOrdersByLotId(@PathVariable Long id){
        List<Orders> orders = parkinglotService.getOrdersByLotId(id);
        if(orders.size() == 0){
            throw new BadRequestException("no orders in the parking lot");
        }
        return orders;
    }

}
