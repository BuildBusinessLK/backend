package com.backend.service;

import com.backend.domain.CampaignGoal;
import com.backend.domain.CampaignStatus;
import com.backend.domain.DeliveryStatus;
import com.backend.domain.RecipientGroupType;
import com.backend.domain.Sector;
import com.backend.dto.email.*;
import com.backend.entity.*;
import com.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailCampaignService {

    private static final Logger log = LoggerFactory.getLogger(EmailCampaignService.class);

    private final RecipientGroupRepository recipientGroupRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignRecipientRepository campaignRecipientRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final AiClientService aiClientService;
    private final MockEmailDispatchService mockEmailDispatchService;
    private final EdbRecipientImporterService edbRecipientImporterService;

    public EmailCampaignService(
            RecipientGroupRepository recipientGroupRepository,
            EmailRecipientRepository emailRecipientRepository,
            EmailCampaignRepository campaignRepository,
            EmailCampaignRecipientRepository campaignRecipientRepository,
            CustomerRepository customerRepository,
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            UserProfileRepository userProfileRepository,
            AiClientService aiClientService,
            MockEmailDispatchService mockEmailDispatchService,
            EdbRecipientImporterService edbRecipientImporterService) {
        this.recipientGroupRepository = recipientGroupRepository;
        this.emailRecipientRepository = emailRecipientRepository;
        this.campaignRepository = campaignRepository;
        this.campaignRecipientRepository = campaignRecipientRepository;
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.aiClientService = aiClientService;
        this.mockEmailDispatchService = mockEmailDispatchService;
        this.edbRecipientImporterService = edbRecipientImporterService;
    }

    @Transactional(readOnly = true)
    public List<RecipientGroupDto> getRecipientGroups(Long userId) {
        List<RecipientGroupDto> groups = new ArrayList<>();

        // 1. EDB System Groups
        List<RecipientGroup> systemGroups = recipientGroupRepository.findAll();
        for (RecipientGroup rg : systemGroups) {
            groups.add(new RecipientGroupDto(
                    rg.getId(),
                    rg.getGroupKey(),
                    rg.getName(),
                    rg.getType(),
                    rg.getSector(),
                    rg.getDescription(),
                    rg.isSystem(),
                    rg.getRecipientCount()
            ));
        }

        // 2. Retail Customer Groups (from current user's business profile)
        if (userId != null) {
            Business business = businessRepository.findByOwner_Id(userId).stream().findFirst().orElse(null);
            if (business != null) {
                List<Customer> customers = customerRepository.findByBusiness_Id(business.getId());
                if (!customers.isEmpty()) {
                    int vipCount = (int) customers.stream().filter(Customer::isVip).count();
                    int frequentCount = (int) customers.stream().filter(Customer::isFrequentBuyer).count();
                    int newCount = (int) customers.stream().filter(Customer::isNew).count();
                    int highSpendingCount = (int) customers.stream().filter(Customer::isHighSpending).count();
                    int discountCount = (int) customers.stream().filter(Customer::isInterestedInDiscounts).count();

                    groups.add(new RecipientGroupDto(
                            -1L,
                            "retail_all",
                            "All Retail Customers (" + business.getBusinessName() + ")",
                            RecipientGroupType.RETAIL_CUSTOMERS,
                            business.getSector(),
                            "All registered and seeded store customers",
                            false,
                            customers.size()
                    ));

                    if (vipCount > 0) {
                        groups.add(new RecipientGroupDto(
                                -2L,
                                "retail_vip",
                                "VIP Customers",
                                RecipientGroupType.RETAIL_CUSTOMERS,
                                business.getSector(),
                                "High value loyal purchasers",
                                false,
                                vipCount
                        ));
                    }
                    if (frequentCount > 0) {
                        groups.add(new RecipientGroupDto(
                                -3L,
                                "retail_frequent",
                                "Frequent Buyers",
                                RecipientGroupType.RETAIL_CUSTOMERS,
                                business.getSector(),
                                "Repeated monthly buyers",
                                false,
                                frequentCount
                        ));
                    }
                    if (newCount > 0) {
                        groups.add(new RecipientGroupDto(
                                -4L,
                                "retail_new",
                                "New Customers",
                                RecipientGroupType.RETAIL_CUSTOMERS,
                                business.getSector(),
                                "Recently registered customers",
                                false,
                                newCount
                        ));
                    }
                    if (highSpendingCount > 0) {
                        groups.add(new RecipientGroupDto(
                                -5L,
                                "retail_high_spending",
                                "High Spending Customers",
                                RecipientGroupType.RETAIL_CUSTOMERS,
                                business.getSector(),
                                "High basket value buyers",
                                false,
                                highSpendingCount
                        ));
                    }
                    if (discountCount > 0) {
                        groups.add(new RecipientGroupDto(
                                -6L,
                                "retail_discounts",
                                "Discount Seekers",
                                RecipientGroupType.RETAIL_CUSTOMERS,
                                business.getSector(),
                                "Customers responsive to promotions",
                                false,
                                discountCount
                        ));
                    }
                }
            }
        }

        return groups;
    }

    @Transactional(readOnly = true)
    public Page<EmailRecipientDto> getRecipients(Long groupId, String search, Pageable pageable) {
        Page<EmailRecipient> page = emailRecipientRepository.searchRecipients(
                groupId,
                (search == null || search.isBlank()) ? null : search.trim(),
                pageable
        );

        List<EmailRecipientDto> dtos = page.getContent().stream().map(r -> {
            EmailRecipientDto dto = new EmailRecipientDto();
            dto.setId(r.getId());
            dto.setGroupId(r.getGroup().getId());
            dto.setGroupName(r.getGroup().getName());
            dto.setName(r.getName());
            dto.setCompanyName(r.getCompanyName());
            dto.setEmail(r.getEmail());
            dto.setPhone(r.getPhone());
            dto.setDistrict(r.getDistrict());
            dto.setCity(r.getCity());
            dto.setAddress(r.getAddress());
            dto.setSector(r.getSector());
            dto.setRecipientType(r.getRecipientType());
            return dto;
        }).toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public EmailGenerateResponse generateEmail(EmailGenerateRequest request, Long userId) {
        if (userId != null) {
            userProfileRepository.findByUser_Id(userId).ifPresent(p -> {
                if (request.getUserName() == null || request.getUserName().isBlank()) {
                    request.setUserName(p.getFullName());
                }
                if (request.getContactPhone() == null || request.getContactPhone().isBlank()) {
                    request.setContactPhone(p.getPhone());
                }
            });

            Business b = businessRepository.findByOwner_Id(userId).stream().findFirst().orElse(null);
            if (b != null) {
                if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                    request.setCompanyName(b.getBusinessName());
                }
                if (request.getSector() == null || request.getSector().isBlank()) {
                    request.setSector(b.getSector() != null ? b.getSector().name() : "COCONUT");
                }
                businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(bp -> {
                    if (request.getTargetAudience() == null || request.getTargetAudience().isBlank()) {
                        request.setTargetAudience(bp.getTargetMarket());
                    }
                    if (request.getKeyOffer() == null || request.getKeyOffer().isBlank()) {
                        request.setKeyOffer(bp.getMarketingGoals());
                    }
                });
            }
        }

        return aiClientService.generateEmail(request);
    }

    @Transactional
    public EmailCampaignDto createCampaign(CreateCampaignRequest request, Long userId) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setUserId(userId);
        campaign.setTitle(request.getTitle());
        campaign.setGoal(request.getGoal() != null ? request.getGoal() : CampaignGoal.GENERAL_ANNOUNCEMENT);
        campaign.setTargetSector(request.getTargetSector());
        campaign.setTargetAudienceSummary(request.getTargetAudienceSummary());
        campaign.setSubject(request.getSubject());
        campaign.setBody(request.getBody());
        campaign.setMock(request.isMock());
        campaign.setStatus(CampaignStatus.READY);

        EmailCampaign savedCampaign = campaignRepository.save(campaign);

        // Snapshot recipients
        List<EmailCampaignRecipient> recipientsToSave = new ArrayList<>();
        Set<String> addedEmails = new HashSet<>();

        // 1. Group IDs (System Recipient Groups)
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            for (Long gid : request.getGroupIds()) {
                if (gid > 0) {
                    List<EmailRecipient> list = emailRecipientRepository.findByGroup_Id(gid);
                    for (EmailRecipient r : list) {
                        if (addedEmails.add(r.getEmail().toLowerCase(Locale.ROOT))) {
                            EmailCampaignRecipient cr = new EmailCampaignRecipient();
                            cr.setCampaign(savedCampaign);
                            cr.setRecipientName(r.getName());
                            cr.setRecipientEmail(r.getEmail());
                            cr.setCompanyName(r.getCompanyName());
                            cr.setStatus(DeliveryStatus.PENDING);
                            recipientsToSave.add(cr);
                        }
                    }
                }
            }
        }

        // 2. Group Keys (EDB groups or retail keys)
        if (request.getGroupKeys() != null && !request.getGroupKeys().isEmpty()) {
            for (String key : request.getGroupKeys()) {
                if (key.startsWith("edb_")) {
                    List<EmailRecipient> list = emailRecipientRepository.findByGroup_GroupKey(key);
                    for (EmailRecipient r : list) {
                        if (addedEmails.add(r.getEmail().toLowerCase(Locale.ROOT))) {
                            EmailCampaignRecipient cr = new EmailCampaignRecipient();
                            cr.setCampaign(savedCampaign);
                            cr.setRecipientName(r.getName());
                            cr.setRecipientEmail(r.getEmail());
                            cr.setCompanyName(r.getCompanyName());
                            cr.setStatus(DeliveryStatus.PENDING);
                            recipientsToSave.add(cr);
                        }
                    }
                } else if (key.startsWith("retail_")) {
                    Business b = businessRepository.findByOwner_Id(userId).stream().findFirst().orElse(null);
                    if (b != null) {
                        List<Customer> customers = customerRepository.findByBusiness_Id(b.getId());
                        List<Customer> filtered = switch (key) {
                            case "retail_vip" -> customers.stream().filter(Customer::isVip).toList();
                            case "retail_frequent" -> customers.stream().filter(Customer::isFrequentBuyer).toList();
                            case "retail_new" -> customers.stream().filter(Customer::isNew).toList();
                            case "retail_high_spending" -> customers.stream().filter(Customer::isHighSpending).toList();
                            case "retail_discounts" -> customers.stream().filter(Customer::isInterestedInDiscounts).toList();
                            default -> customers;
                        };
                        for (Customer c : filtered) {
                            if (addedEmails.add(c.getEmail().toLowerCase(Locale.ROOT))) {
                                EmailCampaignRecipient cr = new EmailCampaignRecipient();
                                cr.setCampaign(savedCampaign);
                                cr.setRecipientName(c.getName());
                                cr.setRecipientEmail(c.getEmail());
                                cr.setCompanyName(c.getName() + " (Retail Customer)");
                                cr.setStatus(DeliveryStatus.PENDING);
                                recipientsToSave.add(cr);
                            }
                        }
                    }
                }
            }
        }

        if (recipientsToSave.isEmpty()) {
            // Safety fallback: attach default EDB Coconut group if none selected
            List<EmailRecipient> defaults = emailRecipientRepository.findByGroup_GroupKey("edb_coconut");
            for (EmailRecipient r : defaults) {
                if (addedEmails.add(r.getEmail().toLowerCase(Locale.ROOT))) {
                    EmailCampaignRecipient cr = new EmailCampaignRecipient();
                    cr.setCampaign(savedCampaign);
                    cr.setRecipientName(r.getName());
                    cr.setRecipientEmail(r.getEmail());
                    cr.setCompanyName(r.getCompanyName());
                    cr.setStatus(DeliveryStatus.PENDING);
                    recipientsToSave.add(cr);
                }
            }
        }

        campaignRecipientRepository.saveAll(recipientsToSave);

        savedCampaign.setTotalRecipients(recipientsToSave.size());
        campaignRepository.save(savedCampaign);

        return mapToDto(savedCampaign);
    }

    @Transactional(readOnly = true)
    public List<EmailCampaignDto> getCampaigns(Long userId) {
        return campaignRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmailCampaignDto getCampaignById(Long campaignId, Long userId) {
        EmailCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found: " + campaignId));
        if (!campaign.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized access to campaign " + campaignId);
        }
        return mapToDto(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignRecipientDto> getCampaignRecipients(Long campaignId, Long userId) {
        getCampaignById(campaignId, userId); // verify ownership
        return campaignRecipientRepository.findByCampaign_Id(campaignId).stream().map(r -> {
            CampaignRecipientDto dto = new CampaignRecipientDto();
            dto.setId(r.getId());
            dto.setRecipientName(r.getRecipientName());
            dto.setRecipientEmail(r.getRecipientEmail());
            dto.setCompanyName(r.getCompanyName());
            dto.setStatus(r.getStatus());
            dto.setOpenedAt(r.getOpenedAt());
            dto.setClickedAt(r.getClickedAt());
            dto.setErrorMessage(r.getErrorMessage());
            return dto;
        }).toList();
    }

    @Transactional
    public CampaignSimulationResponse simulateCampaign(Long campaignId, Long userId) {
        EmailCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found: " + campaignId));
        if (!campaign.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized access to campaign " + campaignId);
        }
        return mockEmailDispatchService.simulateCampaign(campaign);
    }

    public Map<String, Object> sendTestEmail(Long campaignId, String testEmail, Long userId) {
        EmailCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found: " + campaignId));
        if (!campaign.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized access to campaign " + campaignId);
        }
        int sent = mockEmailDispatchService.sendTestEmail(campaign, testEmail);
        return Map.of(
                "success", true,
                "message", "Test email dispatched to " + sent + " authorized test mailbox(es).",
                "campaignId", campaignId,
                "authorizedMailboxes", MockEmailDispatchService.AUTHORIZED_TEST_EMAILS
        );
    }

    public int importEdbDirectory() {
        return edbRecipientImporterService.importEdbDirectory();
    }

    private EmailCampaignDto mapToDto(EmailCampaign c) {
        EmailCampaignDto dto = new EmailCampaignDto();
        dto.setId(c.getId());
        dto.setUserId(c.getUserId());
        dto.setTitle(c.getTitle());
        dto.setGoal(c.getGoal());
        dto.setTargetSector(c.getTargetSector());
        dto.setTargetAudienceSummary(c.getTargetAudienceSummary());
        dto.setSubject(c.getSubject());
        dto.setBody(c.getBody());
        dto.setStatus(c.getStatus());
        dto.setTotalRecipients(c.getTotalRecipients());
        dto.setDeliveredCount(c.getDeliveredCount());
        dto.setOpenedCount(c.getOpenedCount());
        dto.setClickedCount(c.getClickedCount());
        dto.setFailedCount(c.getFailedCount());
        dto.setMock(c.isMock());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setSentAt(c.getSentAt());

        double total = c.getTotalRecipients();
        dto.setDeliveryRate(total > 0 ? Math.round(((double) c.getDeliveredCount() / total) * 1000.0) / 10.0 : 0.0);
        dto.setOpenRate(c.getDeliveredCount() > 0 ? Math.round(((double) c.getOpenedCount() / c.getDeliveredCount()) * 1000.0) / 10.0 : 0.0);
        dto.setClickRate(c.getOpenedCount() > 0 ? Math.round(((double) c.getClickedCount() / c.getOpenedCount()) * 1000.0) / 10.0 : 0.0);

        return dto;
    }
}
