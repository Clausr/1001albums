package dk.clausr.a1001albumsgenerator.tracking;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class Tracking_Factory implements Factory<Tracking> {
  private final Provider<Context> contextProvider;

  public Tracking_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Tracking get() {
    return newInstance(contextProvider.get());
  }

  public static Tracking_Factory create(Provider<Context> contextProvider) {
    return new Tracking_Factory(contextProvider);
  }

  public static Tracking newInstance(Context context) {
    return new Tracking(context);
  }
}
