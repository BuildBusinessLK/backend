package com.backend.controller;

import com.backend.domain.Sector;
import com.backend.entity.Business;
import com.backend.entity.Customer;
import com.backend.repository.BusinessRepository;
import com.backend.repository.CustomerRepository;
import com.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerSeedController {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;

    public CustomerSeedController(BusinessRepository businessRepository, CustomerRepository customerRepository) {
        this.businessRepository = businessRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedCustomers(
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Business business = businessRepository.findByOwner_Id(principal.getId()).stream().findFirst().orElse(null);
        if (business == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Please create a Business Profile first before seeding customer data."));
        }

        // Delete existing customers for this business to avoid duplication
        customerRepository.deleteByBusiness_Id(business.getId());

        Sector sector = business.getSector();
        List<Customer> seededList = new ArrayList<>();
        Random rand = new Random();

        // Sri Lankan Districts & Cities
        String[][] locations = {
            {"Colombo", "Nugegoda"}, {"Colombo", "Colombo 07"}, {"Colombo", "Dehiwala"},
            {"Gampaha", "Negombo"}, {"Gampaha", "Kelaniya"}, {"Gampaha", "Kadawatha"},
            {"Kalutara", "Panadura"}, {"Kalutara", "Horana"},
            {"Kandy", "Peradeniya"}, {"Kandy", "Katugastota"},
            {"Galle", "Hikkaduwa"}, {"Galle", "Karapitiya"},
            {"Matara", "Mirissa"}, {"Jaffna", "Chunnakam"},
            {"Kurunegala", "Kuliyapitiya"}, {"Anuradhapura", "Mihintale"},
            {"Badulla", "Ella"}, {"Ratnapura", "Balangoda"}
        };

        // Realistic Sri Lankan Names
        String[] firstNames = {
            "Saman", "Nimal", "Priyantha", "Sunil", "Ruwan", "Kasun", "Tharindu", "Dinesh",
            "Anura", "Duminda", "Chamil", "Roshan", "Asanka", "Mahesh", "Dilshan",
            "Sunethra", "Chaturi", "Dilani", "Nadeesha", "Hansini", "Sanduni", "Ishara",
            "Piyumi", "Asha", "Thilini", "Kavindi", "Nilmini", "Manori", "Kumari", "Sajini"
        };
        String[] lastNames = {
            "Perera", "Fernando", "Silva", "Jayasinghe", "Senanayake", "Bandara", "Herath",
            "Ranasinghe", "Rathnayake", "Gunasekara", "Karunaratne", "Wijesinghe", "Alwis",
            "Dias", "Cooray", "Rodrigo", "Mendis", "Peiris", "Liyanage", "Darmasena"
        };

        // Sector-specific product categories strictly for supported domains
        String[] categories;
        if (sector == Sector.KITHUL) {
            categories = new String[]{"Pure Kithul Treacle", "Kithul Jaggery", "Kithul Flour", "Kithul Nectar Syrup"};
        } else if (sector == Sector.PALMYRAH) {
            categories = new String[]{"Palmyrah (Thal) Jaggery", "Palmyrah Sugar Candy", "Palmyrah Treacle", "Palmyrah Odiyal Flour", "Palmyrah Pinattu"};
        } else {
            // Default and COCONUT
            categories = new String[]{"Virgin Coconut Oil", "Coconut Flour", "Coconut Milk", "Desiccated Coconut", "Toasted Coconut Chips"};
        }

        int targetCount = 35 + rand.nextInt(15); // Generates between 35 and 50 customers

        for (int i = 0; i < targetCount; i++) {
            String fName = firstNames[rand.nextInt(firstNames.length)];
            String lName = lastNames[rand.nextInt(lastNames.length)];
            String fullName = fName + " " + lName;
            String email = fName.toLowerCase() + "." + lName.toLowerCase() + i + "@example.com";
            
            // Format phone number (+947xxxxxxxx)
            String phone = "+947" + (rand.nextInt(8) + 1) + String.format("%07d", rand.nextInt(10000000));
            
            String[] loc = locations[rand.nextInt(locations.length)];
            String district = loc[0];
            String city = loc[1];
            
            int age = 18 + rand.nextInt(55); // 18 to 72 years old
            int purchaseFreq = 1 + rand.nextInt(15);
            double totalSpent = 500.0 + (rand.nextDouble() * 15000.0);
            
            // Flags
            boolean isVip = totalSpent > 10000.0 && purchaseFreq > 8;
            boolean isFrequent = purchaseFreq > 6;
            boolean isNew = purchaseFreq <= 2 && rand.nextBoolean();
            boolean isInactive = !isNew && rand.nextInt(10) < 2; // 20% chance of inactive
            boolean isHighSpending = totalSpent > 8000.0;
            
            boolean interestedInDiscounts = rand.nextBoolean();
            boolean interestedInNewProducts = rand.nextBoolean();
            
            String category = categories[rand.nextInt(categories.length)];
            
            Customer customer = new Customer();
            customer.setBusiness(business);
            customer.setName(fullName);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setDistrict(district);
            customer.setCity(city);
            customer.setAge(age);
            customer.setPurchaseFrequency(purchaseFreq);
            customer.setTotalSpent(Math.round(totalSpent * 100.0) / 100.0);
            customer.setLastPurchaseDate(LocalDateTime.now().minusDays(rand.nextInt(180)));
            customer.setVip(isVip);
            customer.setNew(isNew);
            customer.setFrequentBuyer(isFrequent);
            customer.setHighSpending(isHighSpending);
            customer.setInactive(isInactive);
            customer.setInterestedInDiscounts(interestedInDiscounts);
            customer.setInterestedInNewProducts(interestedInNewProducts);
            customer.setProductCategory(category);
            customer.setCreatedAt(LocalDateTime.now().minusDays(30 + rand.nextInt(300)));
            
            seededList.add(customer);
        }

        customerRepository.saveAll(seededList);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Seeded realistic sample customer database successfully");
        response.put("count", seededList.size());
        response.put("sector", sector.name());

        return ResponseEntity.ok(response);
    }
}
