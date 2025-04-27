package gov.niem.tools.api.core.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Adds JSON as an accepted media type to support cases where the client requests a different
 * format (like text/csv) and an exception is thrown in JSON.
 */
public class EnsureApplicationJsonNegotiationStrategy extends HeaderContentNegotiationStrategy {

  @NonNull
  @Override
  public List<MediaType> resolveMediaTypes(@NonNull NativeWebRequest request) {

    List<MediaType> mediaTypes;

    try {
      mediaTypes = new ArrayList<>(super.resolveMediaTypes(request));
    }
    catch (Exception exception) {
      mediaTypes = new ArrayList<>();
    }

    boolean includesApplicationJson = mediaTypes.stream()
        .anyMatch(mediaType -> mediaType.includes(MediaType.APPLICATION_JSON));

    if (!includesApplicationJson) {
      mediaTypes.add(MediaType.APPLICATION_JSON);
    }

    return mediaTypes;
  }

}
