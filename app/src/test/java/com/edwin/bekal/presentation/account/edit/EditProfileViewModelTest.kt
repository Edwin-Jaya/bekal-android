package com.edwin.bekal.presentation.account.edit

import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.UpdateCustomerRequestDto
import com.edwin.bekal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val customerApi = mockk<CustomerApi>()
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var viewModel: EditProfileViewModel

    private val sampleProfile = CustomerProfileDto(
        id = "cust-123",
        fullName = "Edwin Jaya",
        email = "edwin@example.com",
        phoneNumber = "081234567890",
        address = "Jl. Sudirman No. 1",
        gender = "MALE",
        nik = "1234567890123456",
        status = "ACTIVE"
    )

    @Before
    fun setUp() {
        viewModel = EditProfileViewModel(customerApi, json, authRepository)
    }

    @Test
    fun `loadProfile success updates form fields and sets uiState to Success`() = runTest {
        coEvery { customerApi.getProfile() } returns ApiEnvelope(
            status = 200,
            success = true,
            message = "Success",
            data = sampleProfile
        )

        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertTrue(state is EditProfileUiState.Success)
        assertEquals(sampleProfile, (state as EditProfileUiState.Success).profile)
        assertEquals("Edwin Jaya", viewModel.fullName.value)
        assertEquals("081234567890", viewModel.phoneNumber.value)
        assertEquals("Jl. Sudirman No. 1", viewModel.address.value)
        assertEquals("MALE", viewModel.gender.value)
    }

    @Test
    fun `loadProfile failure sets uiState to Error`() = runTest {
        coEvery { customerApi.getProfile() } returns ApiEnvelope(
            status = 500,
            success = false,
            message = "Internal Server Error",
            data = null
        )

        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertTrue(state is EditProfileUiState.Error)
        assertEquals("Internal Server Error", (state as EditProfileUiState.Error).message)
    }

    @Test
    fun `saveProfile does nothing when customerId is not yet loaded`() = runTest {
        viewModel.saveProfile()

        assertEquals(SaveUiState.Idle, viewModel.saveState.value)
        coVerify(exactly = 0) { customerApi.updateProfile(any(), any()) }
        coVerify(exactly = 0) { authRepository.updateLocalSession(any()) }
    }

    @Test
    fun `saveProfile success calls updateProfile, updates local session, and sets SaveUiState Success`() = runTest {
        coEvery { customerApi.getProfile() } returns ApiEnvelope(
            status = 200,
            success = true,
            message = "Success",
            data = sampleProfile
        )
        viewModel.loadProfile()

        // Modify fields
        viewModel.fullName.value = "Edwin Updated"
        viewModel.phoneNumber.value = "0899999999"
        viewModel.address.value = "Jl. Thamrin No. 2"
        viewModel.gender.value = "MALE"

        val updatedProfile = sampleProfile.copy(
            fullName = "Edwin Updated",
            phoneNumber = "0899999999",
            address = "Jl. Thamrin No. 2"
        )

        val expectedRequest = UpdateCustomerRequestDto(
            fullName = "Edwin Updated",
            phoneNumber = "0899999999",
            address = "Jl. Thamrin No. 2",
            gender = "MALE"
        )

        coEvery {
            customerApi.updateProfile("cust-123", expectedRequest)
        } returns ApiEnvelope(
            status = 200,
            success = true,
            message = "Profile updated",
            data = updatedProfile
        )

        viewModel.saveProfile()

        assertEquals(SaveUiState.Success, viewModel.saveState.value)
        coVerify(exactly = 1) { customerApi.updateProfile("cust-123", expectedRequest) }
        coVerify(exactly = 1) { authRepository.updateLocalSession(updatedProfile) }
    }

    @Test
    fun `saveProfile failure sets SaveUiState Error`() = runTest {
        coEvery { customerApi.getProfile() } returns ApiEnvelope(
            status = 200,
            success = true,
            message = "Success",
            data = sampleProfile
        )
        viewModel.loadProfile()

        coEvery {
            customerApi.updateProfile("cust-123", any())
        } returns ApiEnvelope(
            status = 400,
            success = false,
            message = "Nomor telepon sudah digunakan",
            data = null
        )

        viewModel.saveProfile()

        val state = viewModel.saveState.value
        assertTrue(state is SaveUiState.Error)
        assertEquals("Nomor telepon sudah digunakan", (state as SaveUiState.Error).message)
        coVerify(exactly = 0) { authRepository.updateLocalSession(any()) }
    }

    @Test
    fun `resetSaveState sets saveState to Idle`() {
        viewModel.resetSaveState()
        assertEquals(SaveUiState.Idle, viewModel.saveState.value)
    }
}
